import decimal
import json

from flask import Flask

from vehicles_data_provider import VehiclesStateDataProvider

app = Flask(__name__)

vehicles_provider = VehiclesStateDataProvider()


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/api/v1/vehicles')
def get_vehicle_states():
    vehicles = vehicles_provider.get_vehicles_states()
    return build_response(
        status_code=200,
        body=json.dumps(vehicles, cls=DecimalEncoder)
    )


def build_response(status_code, body=None):
    result = {
        'statusCode': str(status_code),
        'headers': {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
        },
    }
    if body is not None:
        result['body'] = body

    return result


class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            return (str(o) for o in [o])
        return super(DecimalEncoder, self).default(o)


if __name__ == '__main__':
    app.run()
