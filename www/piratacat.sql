-- phpMyAdmin SQL Dump
-- version 3.2.0.1
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tiempo de generación: 14-12-2010 a las 21:04:32
-- Versión del servidor: 5.1.36
-- Versión de PHP: 5.3.0

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `piratacat`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comments`
--

CREATE TABLE IF NOT EXISTS `comments` (
  `iid` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `pubDate` int(10) NOT NULL,
  `author` varchar(255) COLLATE utf8_spanish2_ci NOT NULL,
  `description` text COLLATE utf8_spanish2_ci NOT NULL,
  PRIMARY KEY (`iid`,`cid`),
  KEY `pubDate` (`pubDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

--
-- Volcar la base de datos para la tabla `comments`
--


-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `config`
--

CREATE TABLE IF NOT EXISTS `config` (
  `key` varchar(255) COLLATE utf8_spanish2_ci NOT NULL DEFAULT '',
  `value` varchar(255) COLLATE utf8_spanish2_ci DEFAULT NULL,
  PRIMARY KEY (`key`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

--
-- Volcar la base de datos para la tabla `config`
--

INSERT INTO `config` (`key`, `value`) VALUES
('lastUpdate', '0');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ideas`
--

CREATE TABLE IF NOT EXISTS `ideas` (
  `status` varchar(10) COLLATE utf8_spanish2_ci NOT NULL,
  `iid` int(10) unsigned NOT NULL,
  `pubDate` int(10) unsigned NOT NULL,
  `title` varchar(255) COLLATE utf8_spanish2_ci NOT NULL,
  `description` text COLLATE utf8_spanish2_ci NOT NULL,
  `queried` int(10) unsigned NOT NULL,
  PRIMARY KEY (`iid`),
  KEY `pubDate` (`pubDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

--
-- Volcar la base de datos para la tabla `ideas`
--


-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `solutions`
--

CREATE TABLE IF NOT EXISTS `solutions` (
  `iid` int(10) unsigned NOT NULL,
  `sid` int(10) unsigned NOT NULL,
  `rsid` int(10) unsigned NOT NULL,
  `pubDate` int(11) NOT NULL,
  `title` varchar(255) COLLATE utf8_spanish2_ci NOT NULL,
  `description` text COLLATE utf8_spanish2_ci NOT NULL,
  `votes` int(10) NOT NULL,
  PRIMARY KEY (`iid`,`sid`),
  KEY `rsid` (`rsid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

--
-- Volcar la base de datos para la tabla `solutions`
--

