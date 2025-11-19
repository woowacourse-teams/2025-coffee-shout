resource "aws_security_group" "this" {
  name        = "${var.name_prefix}-sg"
  description = var.description
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.name_prefix}-sg"
  }
}

# Ingress rules
resource "aws_security_group_rule" "ingress" {
  for_each = { for idx, rule in var.ingress_rules : idx => rule }

  type              = "ingress"
  from_port         = each.value.from_port
  to_port           = each.value.to_port
  protocol          = each.value.protocol
  cidr_blocks       = length(lookup(each.value, "cidr_blocks", [])) > 0 ? lookup(each.value, "cidr_blocks", null) : null
  source_security_group_id = lookup(each.value, "source_security_group_id", null)
  description       = lookup(each.value, "description", null)
  security_group_id = aws_security_group.this.id
}

# Egress rules
resource "aws_security_group_rule" "egress" {
  for_each = { for idx, rule in var.egress_rules : idx => rule }

  type              = "egress"
  from_port         = each.value.from_port
  to_port           = each.value.to_port
  protocol          = each.value.protocol
  cidr_blocks       = length(lookup(each.value, "cidr_blocks", [])) > 0 ? lookup(each.value, "cidr_blocks", null) : null
  source_security_group_id = lookup(each.value, "source_security_group_id", null)
  description       = lookup(each.value, "description", null)
  security_group_id = aws_security_group.this.id
}
