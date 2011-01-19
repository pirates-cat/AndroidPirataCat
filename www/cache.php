<?php
/**
* Clase de configuracion
*/
class genCTs {
	const HTML_CACHE = true;
	const HTML_CACHE_LIFETIME = 1200; // tiempo en segundos de duracion del cache
	const HTML_CACHE_VERBOSE = false; // si lo habilitamos, sabremos que esta ocurriendo.
	const HTML_CACHE_SAVEPATH = "/var/www/virtuals/m.pirata.cat/request/cache/"; // ruta absoluta donde se guardara el cache.
}
/**
 * fnxHtmlCache: cache de pagina completa.
 * Created on 07/05/2008
 * @author ignasi.artigas
 * 
 *	Instalacion: 
 *
 * 	En el script que quieras optimizar:
 *		... script qualquiera que hace echos ...
 *           
 *	A–ades lo siguiente, antes y despues:
 *               
 *		fnxHtmlCache::readCache();
 *		if (!fnxHtmlCache::hayCache()){
 *			... script qualquiera que hace echos ...
 *		}
 *		fnxHtmlCache::savePage();
 *         
 *		Existe la opcion de desactivar el cache dentro del script en caso de necesidad, ejecutando:
 *			fnxHtmlCache::override(); // esto deshabilita el cache en el caso que haya una excepcion
 *
 */
class fnxHtmlCache {

	static private $html;
	static private $ruta;
	static private $cachear = false;
	static private $cacheLoaded = false;
	static private $existe;
	static private $override = false;
	static private $prefix = "";

	/**
	 * Establece en que condiciones se usa el cache.
	 */
	private static function precondiciones() {
		self :: $override = false;
		if (genCTs :: HTML_CACHE && // si esta activo el cache de html
		count($_POST) == 0 //&& // si no nos mandan nada por post
		//!ereg(".php", $_SERVER['REQUEST_URI']) // si no estamos en una pagin sin rewrite
		)
			self :: $cachear = true;
		else
			self :: $cachear = false;
	}

	/**
	 * Devuelve true si se leyo cache de disco, false si no habia nada que leer o caduco.
	 */
	public static function hayCache() {
		return self :: $cacheLoaded;
	}

	/**
	 * Devuelve true si el cache ha caducado
	 */
	private static function cacheCaducado($ruta) {
		self :: $existe = file_exists($ruta);
		if (self :: $existe) {
			self :: $existe = ((time() - microtime() - filemtime($ruta)) < genCTs :: HTML_CACHE_LIFETIME) ? true : false;
		}
		return !self :: $existe;
	}

	/**
	 * Lee la pagina cacheada en disco si se cumplen las precondiciones de lectura
	 */
	public static function readCache($prefix="") {
		self :: setPrefix($prefix);
		self :: precondiciones();
		if (self :: $cachear) {
			self :: $ruta = self :: getRuta();
			if (!self :: cacheCaducado(self :: $ruta)) {
				if (genCTs :: HTML_CACHE_VERBOSE)
					echo "fnxHtmlCache: READING CACHE\n<br/>";
				echo file_get_contents(self :: $ruta);
				self :: $cacheLoaded = true;
			} else {
				if (genCTs :: HTML_CACHE_VERBOSE)
					echo "fnxHtmlCache: NOT READING CACHE\n<br/>";
				self :: $cacheLoaded = false;
				ob_start();
			}
		}
	}

	private function getRuta() {
		return genCTs :: HTML_CACHE_SAVEPATH . self :: $prefix . "_" . md5($_SERVER['REQUEST_URI']);
	}

	/**
	 * Define el prefijo de los ficheros de cache
	 */
	private function setPrefix($prefix="") {
		if ($prefix != "" && preg_match('/^[0-9a-zA-Z_-]{1,30}$/', $prefix)) {
			self :: $prefix = $prefix;
		} else {
			self :: $prefix = "";
		}
	}
	
	/**
	 * Guarda la pagina en disco si se cumplian las condiciones de lectura, pero no habia fichero en disco, o si el fichero habia caducado
	 */
	public static function savePage() {
		if (genCTs :: HTML_CACHE && !self :: $cacheLoaded && self :: $cachear) {
			self :: $html = ob_get_clean();
			if (self :: $cachear && !self :: $override) {
				echo self :: $html;
				if (file_exists(self :: $ruta)) {
					unlink(self :: $ruta);
					if (genCTs :: HTML_CACHE_VERBOSE){
						echo "fnxHtmlCache: CACHE EXPIRED, RESAVING\n<br/>";
					}
				} else {
					if (genCTs :: HTML_CACHE_VERBOSE){
						echo "fnxHtmlCache: SAVING CACHE\n<br/>";
					}
				}
				file_put_contents(self :: $ruta, self :: $html);
			}
			elseif (self :: $override) {
				echo self :: $html;
				if (genCTs :: HTML_CACHE_VERBOSE){
					echo "fnxHtmlCache: NOT SAVING CACHE\n<br/>";
				}
			}
		}
		elseif (genCTs :: HTML_CACHE && genCTs :: HTML_CACHE_VERBOSE){
			 echo "fnxHtmlCache: NO NEED TO SAVE CACHE\n<br/>";
		}
	}

	public static function override() {
		self :: $override = true;
	}
}
