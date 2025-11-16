import json
import urllib3
import os
import boto3
from datetime import datetime
from typing import Dict, Any

# SSM Parameter Store에서 설정 로드
ssm = boto3.client('ssm')

def get_parameter(param_name: str, with_decryption: bool = True) -> str:
    """SSM Parameter Store에서 파라미터 가져오기"""
    try:
        response = ssm.get_parameter(
            Name=param_name,
            WithDecryption=with_decryption
        )
        return response['Parameter']['Value']
    except Exception as e:
        print(f"❌ Failed to get parameter {param_name}: {str(e)}")
        raise

# 환경변수에서 프로젝트/환경 정보 가져오기
PROJECT_NAME = os.environ.get('PROJECT_NAME', 'coffeeshout')
ENVIRONMENT = os.environ.get('ENVIRONMENT', 'prod')

# SSM Parameter Store에서 Slack 설정 로드
SLACK_BOT_TOKEN = get_parameter(f'/{PROJECT_NAME}/{ENVIRONMENT}/slack-bot-token')
SLACK_CHANNEL = get_parameter(f'/{PROJECT_NAME}/{ENVIRONMENT}/slack-channel', with_decryption=False)
SLACK_API_URL = 'https://slack.com/api/chat.postMessage'

def lambda_handler(event, context):
    """
    SNS 이벤트를 받아서 Slack으로 알림을 보내는 Lambda 핸들러
    CloudWatch 알람 처리
    """
    print(f"Processing {len(event.get('Records', []))} SNS records")

    if not SLACK_BOT_TOKEN:
        raise ValueError("SLACK_BOT_TOKEN을 SSM Parameter Store에서 가져올 수 없습니다")

    try:
        for record in event.get('Records', []):
            # SNS 메시지 추출
            sns_record = record['Sns']
            subject = sns_record.get('Subject', 'AWS 알림')
            message = sns_record['Message']

            print(f"SNS Subject: {subject}")
            print(f"SNS Message: {message}")

            # Slack으로 메시지 전송
            send_to_slack(subject, message)

        return {'statusCode': 200, 'body': json.dumps('SUCCESS')}

    except Exception as e:
        print(f"Error: {str(e)}")
        raise

def send_to_slack(subject: str, sns_message: str) -> None:
    """
    Slack Bot Token을 사용해서 메시지 전송
    """
    # Slack 메시지 구성
    slack_message = create_slack_message(subject, sns_message)

    http = urllib3.PoolManager()
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {SLACK_BOT_TOKEN}'
    }

    try:
        response = http.request(
            'POST',
            SLACK_API_URL,
            body=json.dumps(slack_message),
            headers=headers
        )

        print(f"Slack API Response Code: {response.status}")

        if response.status != 200:
            raise Exception(f"HTTP 오류: {response.status}")

        response_data = json.loads(response.data.decode('utf-8'))
        if not response_data.get('ok', False):
            error_msg = response_data.get('error', 'Unknown error')
            raise Exception(f"Slack API 오류: {error_msg}")

        print("✅ Slack 메시지 전송 성공")

    except Exception as e:
        print(f"❌ Slack 전송 실패: {str(e)}")
        raise

def create_slack_message(subject: str, sns_message: str) -> Dict[str, Any]:
    """
    SNS 메시지를 Slack 형식으로 변환
    CloudWatch 알람 메시지 처리
    """
    try:
        # JSON 메시지인지 확인
        if sns_message.strip().startswith('{'):
            message_data = json.loads(sns_message)

            # CloudWatch 알람인지 확인
            if 'AlarmName' in message_data:
                return create_cloudwatch_alarm_message(subject, message_data)
            else:
                return create_simple_message(subject, sns_message)
        else:
            # 단순 텍스트 메시지
            return create_simple_message(subject, sns_message)

    except json.JSONDecodeError:
        # JSON 파싱 실패시 단순 메시지로 처리
        return create_simple_message(subject, sns_message)

def create_cloudwatch_alarm_message(subject: str, alarm_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    CloudWatch 알람 메시지 생성
    """
    alarm_name = alarm_data.get('AlarmName', 'Unknown Alarm')
    new_state = alarm_data.get('NewStateValue', 'Unknown')
    old_state = alarm_data.get('OldStateValue', 'Unknown')
    reason = alarm_data.get('NewStateReason', 'No reason provided')
    timestamp = alarm_data.get('StateChangeTime', datetime.now().isoformat())

    # Trigger 정보 추출
    trigger = alarm_data.get('Trigger', {})
    metric_name = trigger.get('MetricName', 'Unknown')
    threshold = trigger.get('Threshold', 'Unknown')
    namespace = trigger.get('Namespace', 'Unknown')

    # Dimensions 추출 (EC2 InstanceId, RDS DBInstanceIdentifier 등)
    dimensions = trigger.get('Dimensions', [])
    resource_id = 'Unknown'
    for dim in dimensions:
        if dim.get('name') in ['InstanceId', 'DBInstanceIdentifier', 'CacheClusterId', 'LoadBalancer', 'TargetGroup']:
            resource_id = dim.get('value', 'Unknown')
            break

    # 상태에 따른 색상과 이모지
    if new_state == 'ALARM':
        color = 'danger'
        emoji = '🚨'
        state_text = '⚠️ 임계값 초과'
    elif new_state == 'OK':
        color = 'good'
        emoji = '✅'
        state_text = '✅ 정상'
    else:
        color = 'warning'
        emoji = '🔔'
        state_text = new_state

    # 메트릭 이름을 한글로 변환
    metric_display_map = {
        'CPUUtilization': 'CPU 사용률',
        'StatusCheckFailed_Instance': 'EC2 인스턴스 상태 체크',
        'StatusCheckFailed_System': 'EC2 시스템 상태 체크',
        'FreeStorageSpace': 'RDS 여유 스토리지',
        'DatabaseConnections': 'RDS 데이터베이스 연결 수',
        'UnHealthyHostCount': 'ALB Unhealthy 호스트 수',
        'DatabaseMemoryUsagePercentage': 'ElastiCache 메모리 사용률'
    }
    metric_display = metric_display_map.get(metric_name, metric_name)

    return {
        'channel': SLACK_CHANNEL,
        'text': f"{emoji} {metric_display} 알람: {alarm_name}",
        'attachments': [
            {
                'color': color,
                'fields': [
                    {
                        'title': '🎯 리소스',
                        'value': resource_id,
                        'short': True
                    },
                    {
                        'title': '📊 메트릭',
                        'value': metric_display,
                        'short': True
                    },
                    {
                        'title': '🔢 임계값',
                        'value': str(threshold),
                        'short': True
                    },
                    {
                        'title': '📈 상태',
                        'value': f"{old_state} → {state_text}",
                        'short': True
                    },
                    {
                        'title': '🔍 상세 내용',
                        'value': reason,
                        'short': False
                    },
                    {
                        'title': '⏰ 발생 시간',
                        'value': format_timestamp(timestamp),
                        'short': False
                    }
                ]
            }
        ]
    }

def create_simple_message(subject: str, message: str) -> Dict[str, Any]:
    """
    단순 텍스트 메시지 생성
    """
    # Subject에서 이모지 추출
    emoji = '🚨' if '실패' in subject or 'Failed' in subject or 'ALARM' in subject else '🔔'
    color = 'danger' if '실패' in subject or 'Failed' in subject or 'ALARM' in subject else 'good'

    return {
        'channel': SLACK_CHANNEL,
        'text': f"{emoji} {subject}",
        'attachments': [
            {
                'color': color,
                'text': message,
                'mrkdwn_in': ['text']
            }
        ]
    }

def format_timestamp(timestamp_str: str) -> str:
    """
    타임스탬프를 읽기 쉬운 형식으로 변환
    """
    try:
        from datetime import datetime
        # ISO 형식 파싱
        dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
        return dt.strftime('%Y-%m-%d %H:%M:%S UTC')
    except:
        return timestamp_str
