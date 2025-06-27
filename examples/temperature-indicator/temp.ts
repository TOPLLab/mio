@external("env", "chip_delay") declare function chip_delay(value: i32): void;
@external("env", "chip_pin_mode") declare function chip_pin_mode(pin: i32, mode: i32): void;
@external("env", "chip_digital_write") declare function chip_digital_write(pin: i32, value: i32): void;
@external("env", "chip_analog_read") declare function chip_analog_read(pin: i32): i32;

const INPUT = 0;
const OUTPUT = 2;

enum Pin {
    led1 = 10,
    led2 = 11,
    sensor = 21,
}

export function main(): void {
    chip_pin_mode(Pin.led1, OUTPUT)
    chip_pin_mode(Pin.led2, OUTPUT)

    chip_pin_mode(Pin.sensor, INPUT)

    while (true) {
        if (chip_analog_read(Pin.sensor) > 200) {
            chip_digital_write(Pin.led1, 1);
            chip_digital_write(Pin.led2, 0);
        }
        else {
            chip_digital_write(Pin.led1, 0);
            chip_digital_write(Pin.led2, 1);
        }
        chip_delay(10);
    }
}
