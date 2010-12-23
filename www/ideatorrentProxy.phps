<?php

/**
 *  @license GNU/GPL
 *  @author Sergio Arcos
 *  @date 10/12/2010
 * 
 *  -- ideaTorrent proxy-cache RSS to JSON
 */

error_reporting(E_ALL);
set_time_limit(20);


///	RSS_PHP - the PHP DOM based RSS Parser
///	Author: <rssphp.net>
require('rss_php.phps');
$rss = new rss_php();

/// db.php has the private information about our database :-)
$dbFile = 'db.php';
if (!file_exists($dbFile)) {
	$db = mysql_connect('localhost', 'root', '');
	mysql_select_db('piratacat', $db);
} else {
	require($dbFile);
}

/// Full Path of our ideatorrent
$webPath = 'https://xifrat.pirata.cat/ideatorrent/';


require('ideatorrentRSStoJSON.phps');
$idea = new ideatorrentRSStoJSON();
$idea->setRSS($rss);
$idea->setDB($db);
$idea->setWebPath($webPath);


// WHAT DO YOU WANNA DO?

// auto-update each 10minutes
$R = mysql_query("SELECT value FROM config c WHERE c.key='lastUpdate' LIMIT 1", $db);

if (mysql_num_rows($R) > 0) {
	$minTime = time() - (10*60); // 10 minutes
	$row = mysql_fetch_array($R, MYSQL_ASSOC);
	if ($row['value'] < $minTime) {
		$idea->update($minTime);
		mysql_query("UPDATE config c SET value=".time()." WHERE c.key='lastUpdate'", $db);
	}
}

// menu
if (!empty($_GET['time']) && is_numeric($_GET['time']) && $_GET['time']>0)
{
	$minTime = time() - (2*30*24*60*60); // 2 month
	$idea->getIdeasAndSolutions($_GET['time'], $minTime);
}
else if (!empty($_GET['id']) && is_numeric($_GET['id']))
{
	$minTime = time() - (30*60); // 30 minutes
	$idea->getComments($_GET['id'], $minTime);
}
else
{
	echo time();
}

?>
