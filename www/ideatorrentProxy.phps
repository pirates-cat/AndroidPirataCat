<?php

/**
 *  @license GNU/GPL
 *  @author Sergio Arcos
 *  @date 10/12/2010
 * 
 *  -- ideaTorrent proxy-cache RSS to JSON
 */

error_reporting(E_ALL);


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

if (isset($_GET['update']))
{
	$minTime = time() - (2*30*24*60*60); // 2 month
	$idea->update($minTime);
}
else if (!empty($_GET['time']) && is_numeric($_GET['time']) && $_GET['time']>0)
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
