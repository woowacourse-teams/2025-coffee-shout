resource "aws_db_subnet_group" "this" {
  name       = "${var.name_prefix}-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(
    {
      Name = "${var.name_prefix}-subnet-group"
    },
    var.tags
  )
}

resource "aws_db_instance" "this" {
  identifier     = var.name_prefix
  engine         = var.engine
  engine_version = var.engine_version

  # Free tier eligible instance class
  instance_class = var.instance_class

  # Free tier eligible storage (20GB max for free tier)
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = var.storage_type
  storage_encrypted     = var.storage_encrypted

  # Database configuration
  db_name  = var.db_name
  username = var.username
  password = var.password
  port     = var.port

  # Network configuration
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = var.security_group_ids
  publicly_accessible    = var.publicly_accessible

  # Backup configuration
  backup_retention_period = var.backup_retention_period
  backup_window           = var.backup_window
  maintenance_window      = var.maintenance_window

  # Free tier requires single-AZ
  multi_az = var.multi_az

  # Other settings
  skip_final_snapshot       = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.name_prefix}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"
  deletion_protection       = var.deletion_protection
  apply_immediately         = var.apply_immediately

  tags = merge(
    {
      Name = var.name_prefix
    },
    var.tags
  )
}
