# Temperature indicator
The temperature indicator is a very simple program that uses an analog sensor to turn on or off red/blue LEDs, depending on the temperature. If the temperature is above a certain threshold the red LED is turned on and the blue one off and vice versa.

To compile it yourself you will need the [AssemblyScript](https://www.assemblyscript.org/) compiler installed on your system and the [just](https://github.com/casey/just) command runner.

To build the program you then simply run:
```bash
just build
```
