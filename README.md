# MIO Debugger
## Config
To run you will need a `debugger.properties` configuration file, this config currently needs to be positioned at
`~/.wardbg/debugger.properties`. This config specifies where the debugger can find WARDuino, which USB port it should
use, if the debugger should run on an emulator or not and how big the UI should be. An example of this config file can
be found in this repository.

Currently, MIO uses a modified version of WARDuino that has a `--dump-info` argument. This argument is used to get
information about the binary such as which program counters in the binary are right after primitive calls, what the
names are of imported functions, etc... 

## Command line arguments
There are a few command line options:
- `repl` which starts up a debugger repl.
- `debug` which starts up the graphical debugger, this option requires two arguments a `.wasm.map` and `.wasm` file. Example: `debug robotarm.wasm.map robotarm.wasm`.
- `flash` which uploads the program provided as an argument and starts to run it on the microcontroller.
- `run` same thing as flash but after uploading the module it will open a debugger repl.
