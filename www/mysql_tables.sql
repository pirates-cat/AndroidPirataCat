-- phpMyAdmin SQL Dump
-- version 3.2.0.1
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tiempo de generación: 08-12-2010 a las 23:27:39
-- Versión del servidor: 5.1.36
-- Versión de PHP: 5.3.0

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Base de datos: `piratacat`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comentaris`
--

CREATE TABLE IF NOT EXISTS `comentaris` (
  `pid` int(10) unsigned NOT NULL,
  `cid` int(10) unsigned NOT NULL,
  `pubDate` int(10) NOT NULL,
  `author` varchar(255) COLLATE utf8_spanish2_ci NOT NULL,
  `description` text COLLATE utf8_spanish2_ci NOT NULL,
  PRIMARY KEY (`pid`,`cid`),
  KEY `pubDate` (`pubDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `propostes`
--

CREATE TABLE IF NOT EXISTS `propostes` (
  `status` varchar(10) COLLATE utf8_spanish2_ci NOT NULL,
  `pid` int(10) unsigned NOT NULL,
  `pubDate` int(10) unsigned NOT NULL,
  `title` varchar(255) COLLATE utf8_spanish2_ci NOT NULL,
  `description` text COLLATE utf8_spanish2_ci NOT NULL,
  `queried` int(10) unsigned NOT NULL,
  PRIMARY KEY (`pid`),
  KEY `pubDate` (`pubDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `solucions`
--

CREATE TABLE IF NOT EXISTS `solucions` (
  `pid` int(10) unsigned NOT NULL,
  `sid` int(10) unsigned NOT NULL,
  `title` varchar(255) COLLATE utf8_spanish2_ci NOT NULL,
  `description` text COLLATE utf8_spanish2_ci NOT NULL,
  `votes` int(10) NOT NULL,
  PRIMARY KEY (`pid`,`sid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;
