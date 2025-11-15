# 커피빵 프로젝트 메인 Terraform 설정

# 네트워크 모듈 (VPC, Subnet 등)
module "network" {
  source = "./modules/network"

  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
  project_name       = var.project_name
  environment        = var.environment
}

# Compute 모듈 (EC2 인스턴스)
module "compute" {
  source = "./modules/compute"

  vpc_id                 = module.network.vpc_id
  public_subnet_ids      = module.network.public_subnet_ids
  backend_instance_type  = var.backend_instance_type
  frontend_instance_type = var.frontend_instance_type
  project_name           = var.project_name
  environment            = var.environment

  # network 모듈이 먼저 생성되어야 함
  depends_on = [module.network]
}

# Database 모듈 (RDS)
module "database" {
  source = "./modules/database"

  vpc_id                    = module.network.vpc_id
  private_subnet_ids        = module.network.private_subnet_ids
  backend_security_group_id = module.compute.backend_security_group_id
  db_instance_class         = var.db_instance_class
  db_name                   = var.db_name
  db_username               = var.db_username
  db_password               = var.db_password
  project_name              = var.project_name
  environment               = var.environment

  # network와 compute 모듈이 먼저 생성되어야 함
  depends_on = [module.network, module.compute]
}
