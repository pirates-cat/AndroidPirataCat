<?php

/**
 *  @license GNU/GPL
 *  @author Sergio Arcos
 *  @date 19/01/2011
 * 
 *  -- Ideatorrent proxy v2
 */
 
error_reporting(0);
set_time_limit(5);


///	RSS_PHP - the PHP DOM based RSS Parser
///	Author: <rssphp.net>
require('rss_php.phps');
$rss = new rss_php();

// stats
$x = intval(file_get_contents("count.txt"));
$H = fopen("count.txt", "w");
fwrite($H, ($x+1));
fclose($H);

//////////// APANYO
$url = parse_url($_SERVER['REQUEST_URI']);
parse_str($url['query'], $query);
if (isset($query['rss']) && !empty($query['rss']))
	$query['rss'] = null;
$q = "";
foreach ($query as $k => $v) { $q = "$q$k=$v&"; }
$_SERVER['REQUEST_URI'] = substr($q, 0, strlen($q)-1);
////////////

require('cache.php');
fnxHtmlCache::readCache("");
if (!fnxHtmlCache::hayCache()) {
	
	if (isset($_GET['up']))
	{
		showIdea();
	}
	else if (!empty($_GET['id']) && is_numeric($_GET['id']))
	{
		showComments($rss, $_GET['id']);
	}
	else if (isset($_GET['rss']))
	{
		showRSS($rss, $_GET['lang']);
	}
}
fnxHtmlCache::savePage();
exit;




function showIdea() {
	//$str = getContentHttps("https://xifrat.pirata.cat/ideatorrent2json.php");
	$str = file_get_contents("toString.json");
	print($str);
}


function showComments($rss, $id) {
	$V = array();
	$rssStr = getContentHttps('https://xifrat.pirata.cat/ideatorrent/idea/'.$id.'/rss2');
	if ($rssStr != null) {
		$rss->loadRSS($rssStr);
		$rssItems = $rss->getItems();
			
		if (!isset($rssItems['rss'])) {
			$i = 0;
			foreach ($rssItems as $rssItem) {
				$t = str_replace(array('<br />',"\n\n"), '', $rssItem['description']);
				$t = explode('<b>', $t);
				$V[$i]['author'] = substr($rssItem['title'], 13);
				$V[$i]['pubDate']	= strtotime($rssItem['pubDate']);
				$V[$i]['description'] = trim($t[0]);
				$i++;
			}
		}
	}
	print(json_encode($V));
}


function showRSS($rss, $lang) {
	if ($lang == 'es')
	{
		$urls = array(
			0 => 'https://www.partidopirata.es/noticias?format=feed&type=rss',
			3 => 'http://www.nacionred.com/tag/partido-pirata/rss2.xml'
		);
	}
	else
	{
		$urls = array(
			0 => 'http://pirata.cat/bloc/?feed=rss2',
			1 => 'http://gdata.youtube.com/feeds/base/users/PiratesdeCatalunyaTV/uploads?alt=rss&v=2&orderby=published',
			2 => 'http://api.flickr.com/services/feeds/groups_pool.gne?id=1529563@N23&lang=es-es&format=rss_200',
			3 => 'http://www.nacionred.com/tag/partido-pirata/rss2.xml'
		);
	}
	
	$V = array();
	foreach ($urls as $key => $url) {
		$rssStr = getContentHttps($url);
		$rss->loadRSS($rssStr);
		$rssItems = $rss->getItems();
				
		if (!isset($rssItems['rss'])) {
			$i = 0;
			foreach ($rssItems as $rssItem) {
				if ($i>9) continue;
				$v = array();
				$v['id'] = $key;
				$v['link'] = $rssItem['link'];
				$v['pubDate'] = strtotime($rssItem['pubDate']);
				$v['title'] = $rssItem['title'];
				$V[$v['pubDate']] = $v;
				$i++;
			}
		}
	}
	ksort($V, SORT_NUMERIC);
	print(json_encode($V));
}


function getContentHttps($url) {
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 2);
	curl_setopt($ch, CURLOPT_HEADER, false);
	curl_setopt($ch, CURLOPT_AUTOREFERER, true);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
	curl_setopt($ch, CURLOPT_VERBOSE, false);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	$res = curl_exec($ch);
	$res = (curl_getinfo($ch, CURLINFO_HTTP_CODE) == 200) ? $res : null;
	curl_close($ch);
	
	return $res;
}

?>
