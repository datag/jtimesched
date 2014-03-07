# jTimeSched

jTimeSched is a simple and lightweight time tracking tool. You can track elapsed time for tasks and projects and use the data for the recording of time worked.

One aim of jTimeSched is a lean but extremely intuitive GUI and providing only really necessary functionality.

For a complete [list of features](http://kbase.dominik-geyer.de/apps:jtimesched#features) please see the [project website](http://kbase.dominik-geyer.de/apps:jtimesched).


## Download binary release

If you don't want to build the application yourself, you can download and run one of the binary releases containing platform launchers for Linux and Windows.

* [Download latest version](http://www.dominik-geyer.de/files/jTimeSched/jTimeSched-latest.zip)
* [Browse all releases](http://www.dominik-geyer.de/files/jTimeSched/)


## Documentation

The user manual can be found at the [project website](http://kbase.dominik-geyer.de/apps:jtimesched):

* [Running jTimeSched](http://kbase.dominik-geyer.de/apps:jtimesched#running_jtimesched)
* [Using jTimeSched](http://kbase.dominik-geyer.de/apps:jtimesched#using_jtimesched)


## Building from source

jTimeSched can be built using *ant*:

    $ ant


### Build variables (`build.xml`)

* `version` - Application version string written to Manifest and used in the application
* `build` - Build directory
* `dist` - Directory used for generating a release package

You can override the defaults providing the `-D` option to *ant*, e.g. `$ ant -Dversion=my-1.5`

Example: Build release package with version string "my-1.5"

    $ ant -Dversion=my-1.5


### Build targets (`build.xml`)

* `compile`  
Just compile the sources. jTimeSched requires to access certain assets and searches for them in JAR-file, if present, or uses them from a directory with name `data`
* `data`  
Copy assets to the build directory. You can run jTimeSched using `java -cp $[build} de.dominik_geyer.jtimesched.JTimeSchedApp`
* `jar`  
Pack all class files and assets into one single JAR-file, which will be stored at `${dist}/jTimeSched.jar`. Run it with `java -jar ${dist}/jTimeSched.jar`
* `zip`  
Create a release packages (including platform specific launchers). The archive will be stored in `${dist}/jTimeSched-${version}.zip`
* `clean`  
Clean-up everything


Example: Just compile the sources

    $ ant compile


### Eclipse project

The repository provides a basic Eclipse project. You may use the *Import*-wizard of Eclipse.

