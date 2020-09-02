resource "aws_dynamodb_table" "vehicles-states" {
  name           = "VehiclesStates"
  billing_mode   = "PROVISIONED"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "id"
  range_key      = "timestamp"

  attribute {
    name = "id"
    type = "S"
  }

  attribute {
    name = "timestamp"
    type = "N"
  }

  ttl {
    attribute_name = "TimeToExist"
    enabled        = false
  }

  tags = {
    Name        = "vehicles-states"
    Environment = "production"
  }
}