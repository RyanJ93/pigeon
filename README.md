# Pigeon

Pigeon is a simple email-like messaging application developed as an academic project using Java and JavaFX.<br />
It allows you to exchange messages in the same fashion you are used to with regular emails.

## Requirements

- Java 15 or greater
- Gradle 7

## Building the client

The project's dependencies and building strategy is managed by Gradle, you can run the client by running the following command:

```bash
gradle run
```

Similarly you can build a "fatjar" using the following command:

```bash
gradle jar
```

It will produce a .jar file located at `build/libs/pigeon-0.0.1.jar`.

### Login

When logging in you must provide both the username and the hostname where you are intended to connect to, for instance: username@hostname.com

## Building the server

Pigeon server is located under `server/pigeon-server`.<br />
Like the main application, Gradle has been used to handle server's dependencies as well.<br />
You can run the server, again, invoking the task "run" and in the same way you can build a "fatjar" invoking the "jar" task.<br />
Server will listen on port 2898, so you may need to adjust your firewall settings to allow clients to connect through that port.<br />
The Pigeon server can be run without a GUI too by passing the --no-gui parameter, useful when running on a CLI-only server.

### The Pigeon server as a Docker image

You can build and then run a Docker image out of this project: all you need is to invoke the `docker build` command on the server directory and then you are free to run the image built. <br />
You may want to change the storage directory when running as a Docker container, to do that simply mount your directory as a volume, here's an example:

```bash
docker run -p 2898:2898 -v /path/to/storage/dir:/storage -d --name pigeon-server enricosola/pigeon-server:latest
```

If you don't want to build your own Docker image you can use a pre-built image, have a look at [this page](https://hub.docker.com/repository/docker/enricosola/pigeon-server) for more information.

### Creating users
You can add new Pigeon users by running the server with the `--useradd` option plus the username and password for the user to be added, for instance:

```bash
java -jar pigeon-server.jar --useradd "username" "password"
```

Or using Gradle:

```bash
./gradlew run --args="--useradd \"username\" \"password\""
```

If you are running the server as a Docker container you can add new users using the following command:

```bash
docker exec pigeon-server pigeon_useradd "username" "password"
````

### Changing user password

Similarly, you can change user passwords using the `change-password` option, usage is the following:

```bash
java -jar pigeon-server.jar --change-password "username" "new password"
```

This option's Docker counterpart is the command `pigeon_change_password`.<br />
User deletion has not been implemented yet.

### Sentry support

The Pigeon server supports the Sentry error tracking platform: all you need to integrate the server with Sentry is your project DSN, declare an environment variable called `PIGEON_SENTRY_DSN` containing your DSN and then start the server.<br />
If your running the server as a Docker container you may set the environment variable as docker run parameter, here's an example:

```bash
docker run -p 2898:2898 -e PIGEON_SENTRY_DSN="YOUR SENTRY DSN HERE" -d --name pigeon-server enricosola/pigeon-server:latest
```

## Requirements

To run and work with this project you need to have Java version 15 or greater installed on your machine, additionally Gradle version 7 is required as well.

## Other considerations

Currently, messages can be sent among the same server users, cross-server messaging may be added in the future.
Additionally, messages are stored as files, while it may sound like a bad practice (and I think that too), it was an explicit requirement for this project. MongoDB support will be added in a near future alongside the file storage system.

Tested on Apple macOS and RedHat Enterprise Linux 8, currently not tested on Microsoft Windows.

Developed with ❤️ by [Enrico Sola](https://www.enricosola.com).
