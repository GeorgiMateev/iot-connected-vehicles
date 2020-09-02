import boto3 as boto3
from boto3.dynamodb.types import TypeDeserializer


class VehiclesStateDataProvider:
    def __init__(self):
        dynamodb = boto3.resource('dynamodb')
        self.table = dynamodb.Table('VehiclesStates')
        self.deserializer = TypeDeserializer()

    def get_vehicles_states(self):
        response = self.table.scan(
            Select='ALL_ATTRIBUTES',
            Limit=10,
        )

        items = response['Items']
        deserialized = [{
            'id': item['id'],
            'timestamp': str(item['timestamp']),
            'latitude': str(item['latitude']),
            'longitude': str(item['longitude']),
        } for item in items]

        return deserialized
