<?php

/**
 *  @license GNU/GPL
 *	@author Sergio Arcos
 *  @date 08/11/2010
 */

// -- CONFIG

error_reporting(E_ALL);

require('rss_php.php');

$rss = new rss_php();

$url = array(
	'waiting' => "https://xifrat.pirata.cat/ideatorrent/ideas_in_preparation/latest_submissions/rss2",
	'voting' => "https://xifrat.pirata.cat/ideatorrent/latest_ideas/rss2",
	'developing' => "https://xifrat.pirata.cat/ideatorrent/ideas_in_development/rss2",
	'done' => "https://xifrat.pirata.cat/ideatorrent/implemented_ideas/rss2"
);

$db = mysql_connect("localhost", "root", "");
mysql_select_db("piratacat", $db);

$miniumTime = time() - (3*30*24*60*60); // 1 month
$miniumCom = time() - (30*60);			// 30 minutes



// -- START

// ?time=[(int)value]
if (!empty($_GET['time']) && is_numeric($_GET['time']) && $_GET['time']>0) {
	$propostes = array();
	$solucions = array();
	
	$R = query("
		SELECT status, pid, pubDate, title, description
		FROM propostes
		WHERE pubDate>".$_GET['time']." AND pubDate>$miniumTime
		ORDER BY pubDate DESC");

	$pid = array();
    while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
		$pid[] = $r['pid'];
        $propostes[] = $r;
    }
    
    if (count($pid)>0) {
		$R = query("
			SELECT pid, sid, title, description, votes
			FROM solucions
			WHERE pid IN (".implode(',', $pid).")
			ORDER BY pid, sid ASC");
		
		if ($R) {
			while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
				$solucions[] = $r;
			}
		}
	}
    
    print(json_encode(array(
		'propostes' => $propostes,
		'solucions' => $solucions)));
}

// ?id=[(int)value]
else if (!empty($_GET['id']) && is_numeric($_GET['id'])) {
	$comentaris = array();

    $R = query("
		SELECT queried
		FROM propostes
		WHERE pid=".$_GET['id']."
		LIMIT 1");
	
	if (mysql_num_rows($R) == 1) {
		$r = mysql_fetch_array($R, MYSQL_ASSOC);
		
		if ($miniumCom > $r['queried']) {
			updateComment($_GET['id']);
		}
		
		$R = query("
			SELECT pid, cid, pubDate, author, description
			FROM comentaris
			WHERE pid=".$_GET['id']."
			ORDER BY cid DESC");
			
		if ($R) {
			while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
				$comentaris[] = $r;
			}
		}
	}
	
	print(json_encode(array(
		'comentaris' => $comentaris)));
}

// ?update
else if (isset($_GET['update'])) {
	update(false);
}
else {
	// testing
	echo time();
}

exit;
// -- END


function update($all=false) {
	global $url, $rss;
	
	foreach ($url as $k => $u) {
		$rssStr = get_https_rss($u);
		$rss->loadRSS($rssStr);
		$rssItems = $rss->getItems();
		
		if (isset($rssItems['rss'])) {
			continue;
		}
		
		foreach ($rssItems as $rssItem) {
			$t = str_replace(array('<br />',"\n\n"), '', $rssItem['description']);
			$t = explode('<b>', $t);
			$V = array();
			$V['title']		= end(explode(' ', $rssItem['title'], 2));
			$V['id']		= substr($rssItem['link'], 43, -1);
			$V['pubDate']	= strtotime($rssItem['pubDate']);
			$V['description'] = trim($t[0]);
			
			insertProposta($k, $V['id'], $V['pubDate'], $V['title'], $V['description']);

			for ($i=1; $i<count($t); $i++) {
				$aux = explode('</b>', $t[$i], 2);
				$aux[0] = explode(' ', $aux[0], 5);
				$V[$i]['votes']			= substr($aux[0][0], 1);
				$V[$i]['title']			= trim(end($aux[0]));
				$V[$i]['description']	= trim($aux[1]);
				
				insertSolucio($V['id'], $i, $V[$i]['title'], $V[$i]['description'], $V[$i]['votes']);
			}
			
			if ($all == true) {
				updateComment($V['id']);
			}
		}
	}
}


function updateComment($id) {
	global $url, $rss;
	
	if (!is_numeric($id) && $id>0) {
		return;
	}
	
	$rssStr = get_https_rss('https://xifrat.pirata.cat/ideatorrent/idea/'.$id.'/rss2');
	$rss->loadRSS($rssStr);
    $rssItems = $rss->getItems();
    
    if (isset($rssItems['rss'])) {
		return;
	}
    
    $i = 0;
    foreach ($rssItems as $rssItem) {
		$t = str_replace(array('<br />',"\n\n"), '', $rssItem['description']);
		$t = explode('<b>', $t);
		$V = array();
		$V['author'] = substr($rssItem['title'], 13);
		$V['pubDate']	= strtotime($rssItem['pubDate']);
		$V['description'] = trim($t[0]);
		
		insertComentari($id, $i, $V['pubDate'], $V['author'], $V['description']);
		$i++;
	}
}


function insertProposta($status, $pid, $pubDate, $title, $description) {
	$title = addslashes($title);
	$description = addslashes($description);
	
	query("
		INSERT INTO propostes (status, pid, pubDate, title, description)
		VALUES ('$status', $pid, $pubDate, '$title', '$description')
		ON DUPLICATE KEY UPDATE status='$status', pubDate=$pubDate, title='$title', description='$description'");
}


function insertSolucio($pid, $sid, $title, $description, $votes) {
	$title = addslashes($title);
	$description = addslashes($description);
	
	query("
		INSERT INTO solucions (pid, sid, title, description, votes)
		VALUES ($pid, $sid, '$title', '$description', $votes)
		ON DUPLICATE KEY UPDATE title='$title', description='$description', votes='$votes'");
}


function insertComentari($pid, $cid, $pubDate, $author, $description) {
	$author = addslashes($author);
	$description = addslashes($description);
	
	query("
		INSERT INTO comentaris (pid, cid, pubDate, author, description)
		VALUES ($pid, $cid, $pubDate, '$author', '$description')
		ON DUPLICATE KEY UPDATE pubDate=$pubDate, author='$author', description='$description'");
	
	query("
		UPDATE propostes
		SET queried=".time()."
		WHERE pid=$pid
		LIMIT 1");
}



function query($str) {
	global $db;
	
	$R = mysql_query($str, $db);
	
	if (!$R) {
		die('Invalid query: ' . mysql_error());
	}
	
	return $R;
}


function get_https_rss($url) {
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
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
