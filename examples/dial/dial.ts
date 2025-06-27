@external("env", "chip_delay") declare function chip_delay(value: i32): void;
@external("env", "chip_pin_mode") declare function chip_pin_mode(pin: i32, mode: i32): void;
@external("env", "chip_digital_write") declare function chip_digital_write(pin: i32, value: i32): void;
@external("env", "chip_digital_read") declare function chip_digital_read(pin: i32): i32;
@external("env", "drive_motor_degrees") declare function drive_motor_degrees(motor_index: i32, angle: i32, speed: i32): void;
@external("env", "setup_uart_sensor") declare function setup_uart_sensor(sensor_index: i32, mode: i32): void;
@external("env", "color_sensor") declare function color_sensor(sensor_index: i32): i32;
@external("env", "print_int") declare function print_int(value: i32): i32;

enum Pin {
  powerSupply = 60,
  motorABDriverSleep = 46,
  led1 = 45,
  led2 = 56,
  led3 = 39,
  sw5 = 20,
  sw1 = 21
}

enum InputPort {
  portA = 0,
  portB,
  portC,
  portD
}

enum OutputPort {
  port1 = 0,
  port2,
  port3,
  port4
}

const LOW = 0;
const HIGH = 1;

enum PinMode {
  input = 0,
  output = 2
}

enum ColourSensorMode {
  reflect = 0,
  ambient,
  colour
}

enum Colour {
  none = 0,
  blue = 2,
  green = 3,
  yellow = 4,
  red = 5,
  white = 6,
}

const mapping = [0, 0, 3, 2, 4, 1, 0, 0, 0, 0];

function setup_obb(): void {
  // Configure and enable power supply pin (active low).
  chip_pin_mode(Pin.powerSupply, PinMode.output)
  chip_delay(500) // Wait for 500ms to avoid current spike.
  chip_digital_write(Pin.powerSupply, LOW)

  // Disable sleep on motor AB driver.
  chip_pin_mode(Pin.motorABDriverSleep, PinMode.output)
  chip_digital_write(Pin.motorABDriverSleep, HIGH)

  // Disable sleep on motor CD driver.
  chip_pin_mode(78, PinMode.output)
  chip_digital_write(78, HIGH)
}

export function main(): i32 {
  setup_obb()
 
  // Setup colour sensor.
  setup_uart_sensor(InputPort.portA, ColourSensorMode.colour)

  const MAX_SPEED = 10000
  let last_colour = 0

  while(true) {
    let value = mapping[color_sensor(0)]
 
    if (value != last_colour) {
      drive_motor_degrees(3, (value - last_colour) * -180 * 6, MAX_SPEED)
    }
    last_colour = value
    chip_delay(100)
  }

  return 0
}

