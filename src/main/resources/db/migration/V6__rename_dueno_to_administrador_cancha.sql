-- Primero eliminar la restricción CHECK existente en la tabla usuarios
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_role_check;

-- Agregar nueva restricción CHECK que permita ADMINISTRADOR_CANCHA
ALTER TABLE usuarios ADD CONSTRAINT usuarios_role_check CHECK (role IN ('JUGADOR', 'DUENO', 'ADMINISTRADOR_CANCHA'));

-- Actualizar el role en la tabla usuarios
UPDATE usuarios SET role = 'ADMINISTRADOR_CANCHA' WHERE role = 'DUENO';

-- Actualizar la restricción CHECK para eliminar DUENO ya que no se usará más
ALTER TABLE usuarios DROP CONSTRAINT usuarios_role_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_role_check CHECK (role IN ('JUGADOR', 'ADMINISTRADOR_CANCHA'));

-- Verificar si la columna dueno_id existe y renombrarla
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'canchas' AND column_name = 'dueno_id'
    ) THEN
        ALTER TABLE canchas RENAME COLUMN dueno_id TO administrador_cancha_id;
    END IF;
END $$;

-- Verificar si la tabla duenos existe y administradores_cancha NO existe, entonces renombrar
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'duenos')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'administradores_cancha')
    THEN
        ALTER TABLE duenos RENAME TO administradores_cancha;
    END IF;
END $$;
