-- Script para resetear la base de datos playmatch
-- ADVERTENCIA: Este script eliminará TODOS los datos

-- Desconectar todas las conexiones activas
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'playmatch'
  AND pid <> pg_backend_pid();

-- Eliminar la base de datos
DROP DATABASE IF EXISTS playmatch;

-- Recrear la base de datos
CREATE DATABASE playmatch;
