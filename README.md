# Pigeon

Pigeon is a simple email-like messaging application developed as an academic project using Java and JavaFX.<br />
It allows you to exchange messages in the same fashion you are used to with regular emails.

## Building the client

The project's dependencies and building strategy is managed by Gradle, a Gradle wrapper is included too.<br />
You can run the application invoking the task "run", for instance:

```bash
./gradlew run
```

Or on Windows (PowerShell):

```powershell
.\gradlew run
```

Similarly, you can build a "fatjar" invoking the "jar" task.

### Login

When logging in you must provide both the username and the hostname where you are intended to connect to, for instance: username@hostname.com

## Building the server

Pigeon server is located under server/pigeon-server.<br />
Like the main application, Gradle has been used to handle server's dependencies as well.<br />
You can run the server, again, invoking the task "run" and in the same way you can build a "fatjar" invoking the "jar" task.<br />
Server will listen on port 2898, so you may need to adjust your firewall settings to allow clients to connect through that port.<br />
The Pigeon server can be run without a GUI too by passing the --no-gui parameter, useful when running on a CLI-only server.

### The Pigeon server as a Docker image
You can build and then run a Docker image out of this project: all you need is to invoke the `docker build` command on the server directory and then you are free to run the image built. <br />
You may want to change the storage directory when running as a Docker container, to do that simply mount your directory as a volume, here's an example:

```bash
docker run -v /path/to/storage/dir:/app/storage -d --name pigeon-server pigeon-server:latest
```

### Creating users
You can add new Pigeon users by running the server with the `--useradd` option plus the username and password for the user to be added, for instance:

```bash
java -jar pigeon-server.jar --useradd "username" "password"
```

Or using Gradle:

```bash
./gradlew run --args="--useradd \"username\" \"password\""
```

### Sentry support
The Pigeon server supports the Sentry error tracking platform: all you need to integrate the server with Sentry is your project DSN, declare an environment variable called `PIGEON_SENTRY_DSN` containing your DSN and then start the server.<br />
If your running the server as a Docker container you may set the environment variable as docker run parameter, here's an example:

```bash
docker run -e PIGEON_SENTRY_DSN="YOUR SENTRY DSN HERE" -d --name pigeon-server pigeon-server:latest
```

### Data storage
Server stores its data under "storage" directory, you may set your own directory when running the server as a Docker image,

## Other considerations

Currently, messages can be sent among the same server users, cross-server messaging may be added in the future.
Additionally, messages are stored as files, while it may sound like a bad practice (and I think that too), it was an explicit requirement for this project. MongoDB support will be added in a near future alongside the file storage system.

Developed with ❤️ by [Enrico Sola](https://www.enricosola.com).
