# MIO Debugger
The MIO _/maː.joː/_ debugger is a first of its kind multiverse debugger designed for debugging WebAssembly programs on the WARDuino virtual machine.

## Building
MIO uses the Gradle build system. To build a jar you can simply run `./gradlew fatjar`. The debugger can then be started using `java -jar mio.jar` in the `build/libs` directory. To run MIO you will need Java 22 or higher.

> [!IMPORTANT]
> Before running the debugger you will need to create a configuration as specified below.

## Configuration
To use the debugger you will need a `debugger.properties` configuration file, this config currently needs to be positioned at
`~/.mio/debugger.properties`. This config specifies where the debugger can find WARDuino, which USB port it should
use, if the debugger should run on an emulator or not and how big the UI should be. An example of this config file can
be found [here](debugger.properties).

> [!NOTE]
> Currently, MIO uses a modified version of WARDuino that has a `--dump-info` argument. This argument is used to get
information about the binary such as which program counters in the binary are right after primitive calls, what the
names are of imported functions, etc... 
> 
> This custom argument is available on the `feat/symbolic-templated` branch. This branch also houses the code for the concolic execution engine used for suggesting interesting paths.

## Command line arguments
MIO has various command line arguments that can be used to flash and debug programs. We list these options here:
- `repl` which starts up a debugger repl.
- `debug` which starts up the graphical debugger, this option requires two arguments a `.wasm.map` and `.wasm` file. Example: `debug robotarm.wasm.map robotarm.wasm`.
- `flash` which uploads the program provided as an argument and starts to run it on the microcontroller.
- `run` same thing as flash but after uploading the module it will open a debugger repl.

If no option is specified, MIO will open a welcome screen allowing you to select the program you would like to debug using a graphical interface. This graphical window works similarly to the `debug` option.
