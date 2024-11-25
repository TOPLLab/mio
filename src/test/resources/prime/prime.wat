(module $prime.wasm
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func))
  (func $is_prime (type 0) (param i32) (result i32)
    (local i32)
    block  ;; label = @1
      local.get 0
      i32.const 2
      i32.gt_u
      br_if 0 (;@1;)
      local.get 0
      i32.const 2
      i32.eq
      return
    end
    local.get 0
    i32.const 3
    i32.rem_u
    local.set 1
    block  ;; label = @1
      local.get 0
      i32.const 1
      i32.and
      i32.eqz
      br_if 0 (;@1;)
      local.get 1
      i32.eqz
      br_if 0 (;@1;)
      block  ;; label = @2
        local.get 0
        i32.const 25
        i32.ge_u
        br_if 0 (;@2;)
        i32.const 1
        return
      end
      i32.const 5
      local.set 1
      loop  ;; label = @2
        local.get 0
        local.get 1
        i32.rem_u
        i32.eqz
        br_if 1 (;@1;)
        local.get 0
        local.get 1
        i32.const 2
        i32.add
        i32.rem_u
        i32.eqz
        br_if 1 (;@1;)
        local.get 1
        i32.const 6
        i32.add
        local.tee 1
        local.get 1
        i32.mul
        local.get 0
        i32.le_u
        br_if 0 (;@2;)
      end
      i32.const 1
      return
    end
    i32.const 0)
  (func $_main (type 1)
    (local i32 i32 i32)
    loop  ;; label = @1
      i32.const 1
      local.set 0
      i32.const 0
      local.set 1
      loop  ;; label = @2
        local.get 0
        i32.const 0
        local.get 0
        call $is_prime
        select
        local.set 2
        local.get 0
        i32.const 1
        i32.add
        local.set 0
        local.get 2
        local.get 1
        i32.add
        local.tee 1
        i32.const 13374242
        i32.lt_u
        br_if 0 (;@2;)
        br 1 (;@1;)
      end
    end)
  (func $_start (type 1))
  (table (;0;) 1 1 funcref)
  ;; (memory (;0;) 2)
  ;;(global $__stack_pointer (mut i32) (i32.const 66560))
  ;;(export "memory" (memory 0))
  (export "main" (func $_main))
  (export "_start" (func $_start)))
