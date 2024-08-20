# Health Sync

Synchronizes Health Data from api.rdcl.dev to Garmin Connect.

## Setting up

First, set up Garth to fetch a Garmin Connect access token:

```shell
python3 -m venv .venv
echo '*' > .venv/.gitignore
.venv/bin/python -m pip install garth
.venv/bin/python -m pip install pyyaml
```

Next, create a file `.garth/auth.yaml` with the following contents:

```yaml
auth:
  username: YOUR_GARMIN_CONNECT_USERNAME
  password: YOUR_GARMIN_CONNECT_PASSWORD
```

Once this file is created, an access token can be fetched using:

```shell
.venv/bin/python auth.py
```

Once this is done, the application can be run.
First, set the following environment variables:

| env               | description                                                                                         |
|-------------------|-----------------------------------------------------------------------------------------------------|
| GARTH_OAUTH2_PATH | The path to the oauth2_token.json created by Garth. Defaults to `.garth/session/oauth2_token.json`. |
| DB_DRIVER         | The database driver to be used. Defaults to `postgresql`.                                           |
| DB_HOST           | The database host to be used. Defaults to `localhost`.                                              |
| DB_PORT           | The database port to be used. Defaults to `5432`.                                                   |
| DB_USER           | The username with which to connect to the database. Must be set manually.                           |
| DB_PASSWORD       | The password with which to connect to the database. Must be set manually.                           |
| DB_NAME           | The name of the database to connect to. Must be set manually.                                       |

After these settings have been configured correctly, the application can be run using:

```shell
# Fetch _all_ data from the database. This could take a lot of time!
./gradlew run

# Fetch data newer than a specific date.
./gradlew run --args='2024-08-01'

# Fetch data up to a specific date.
./gradlew run --args='null 2024-08-01'

# Fetch data between specific dates.
./gradlew run --args='2024-07-01 2024-08-01'

# Fetch data for the last seven days.
./gradlew run --args="$(date --date='7 days ago' '+%Y-%m-%d')"
```
