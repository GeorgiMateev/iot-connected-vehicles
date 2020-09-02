provider "aws" {
  region = local.region
}

resource "aws_iot_thing" "vehicle" {
  name = "vehicle"

  attributes = {

  }
}

resource "aws_iot_topic_rule" "real-time-processing" {
  name        = "${local.project_prefix}_rtp"
  description = "Used for real time information gathering"
  enabled     = true
  sql         = "SELECT * FROM 'topic/${local.states_topic}'"
  sql_version = "2016-03-23"

  kinesis {
    role_arn    = aws_iam_role.iot.arn
    stream_name = aws_kinesis_stream.vehicles.name
    partition_key = "$${newuuid()}"
  }
}

resource "aws_kinesis_stream" "vehicles" {
  name             = "${local.states_topic}-stream"
  shard_count      = 1
  retention_period = 24
}

resource "aws_iam_role" "iot" {
  name = "${local.project_prefix}-iot-role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "iot.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "iot_kinesis_policy" {
  name = "${local.project_prefix}-iot-kinesis-policy"
  role = aws_iam_role.iot.id

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "kinesis:PutRecord"
      ],
      "Resource": [
        "${aws_kinesis_stream.vehicles.arn}"
      ]
    }
  ]
}
EOF
}