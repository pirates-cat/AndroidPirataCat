<?php

/**
 *  @license GNU/GPL
 *  @author Sergio Arcos
 *  @date 10/12/2010
 * 
 *  -- ideaTorrent proxy-cache RSS to JSON
 * 
 * ideaTorrent: Your RSS system SUCKS a lot! What were you thinking when you coded it?
 */

class ideatorrentRSStoJSON {
	
	private $webCateg = array(
			'waiting'		=> 'ideas_in_preparation/latest_submissions/rss2',
			'voting'		=> 'latest_ideas/rss2',
			'developing'	=> 'ideas_in_development/rss2',
			'done'			=> 'implemented_ideas/rss2'
		);
			
	private $rss;
	private $db;
	private $webPath;
	
	
	// -- PUBLIC
	
	public function setRSS($externalRss) {
		$this->rss = $externalRss;
	}
	
	public function setDB($externalDb) {
		$this->db = $externalDb;
	}
	
	public function setWebPath($externalWebPath) {
		$this->webPath = $externalWebPath;
	}
	

	public function update($minTime) {
		
		foreach ($this->webCateg as $category => $url) {
			$rssStr = $this->get_https_rss($this->webPath.$url);
			
			if (empty($rssStr)) {
				continue;
			}
			
			$this->rss->loadRSS($rssStr);
			$rssItems = $this->rss->getItems();
			
			if (isset($rssItems['rss'])) {
				continue;
			}
			
			foreach ($rssItems as $rssItem) {
				$t = str_replace(array('<br />',"\n\n"), '', $rssItem['description']);
				$t = explode('<b>', $t);
				$V = array();
				$V['title']	= end(explode(' ', $rssItem['title'], 2));
				$V['id'] = substr($rssItem['link'], 43, -1);
				$V['pubDate'] = strtotime($rssItem['pubDate']);
				$V['description'] = trim($t[0]);
				
				if ($V['pubDate'] > $minTime) {
					echo $V['id'].": ";
					$this->insertIdea($category, $V['id'], $V['pubDate'], $V['title'], $V['description']);
				
					for ($i=1; $i<count($t); $i++) {
						$aux = explode('</b>', $t[$i], 2);
						$aux[0] = explode(' ', $aux[0], 5);
						$V[$i]['votes'] = substr($aux[0][0], 1);
						$V[$i]['title'] = trim(end($aux[0]));
						$V[$i]['description'] = trim($aux[1]);
						
						echo $i.", ";
						$this->insertSolution($V['id'], $i, $V[$i]['title'], $V[$i]['description'], $V[$i]['votes']);
					}
					echo "<br />\n";
				}
			}
		}
	}
	
	
	public function getIdeasAndSolutions($from, $minTime) {
		$ideas = array();
		$solutions = array();
		$votes = array();
		
		// ideas
		$R = $this->query("
			SELECT status, iid, pubDate, title, description
			FROM ideas
			WHERE pubDate>$from AND pubDate>$minTime
			ORDER BY pubDate DESC");

		$iid = array(0);
		while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
			$iid[] = $r['iid'];
			$ideas[] = $r;
		}
		
		// solutions
		$R = $this->query("
			SELECT iid, sid, title, description, votes
			FROM solutions
			WHERE iid IN (".implode(',', $iid).") OR (pubDate>$from AND pubDate>$minTime)
			ORDER BY iid, sid ASC");
		
		if ($R) {
			while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
				$solutions[] = $r;
			}
		}
		
		// votes
		$R = $this->query("
			SELECT s.iid, s.sid, s.votes
			FROM solutions s, ideas i
			WHERE i.pubDate>$minTime AND i.iid = s.iid
			ORDER BY i.pubDate DESC");

		while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
			$votes[] = $r;
		}
		
		print(json_encode(array(
			'ideas' => $ideas,
			'solutions' => $solutions,
			'votes' => $votes)));
	}


	public function getComments($iid, $minTime) {
		$comentaris = array();

		$R = $this->query("
			SELECT queried
			FROM ideas
			WHERE iid=".$iid."
			LIMIT 1");
		
		if (mysql_num_rows($R) == 1) {
			$r = mysql_fetch_array($R, MYSQL_ASSOC);
			
			if ($minTime > $r['queried']) {
				$this->updateComment($iid);
			}
			
			$R = $this->query("
				SELECT iid, cid, pubDate, author, description
				FROM comments
				WHERE iid=".$iid."
				ORDER BY cid ASC");
				
			if ($R) {
				while ($r = mysql_fetch_array($R, MYSQL_ASSOC)) {
					$comentaris[] = $r;
				}
			}
		}
		
		print(json_encode(array(
			'comments' => $comentaris)));
	}



	// -- PRIVATE

	private function updateComment($iid) {
		global $url, $rss;
		
		if (!is_numeric($iid) && $iid>0) {
			return;
		}
		
		$rssStr = $this->get_https_rss($this->webPath.'/idea/'.$iid.'/rss2');
		
		if (empty($rssStr)) {
			continue;
		}
			
		$this->rss->loadRSS($rssStr);
		$rssItems = $this->rss->getItems();
		
		if (!isset($rssItems['rss'])) {
			$i = 0;
			foreach ($rssItems as $rssItem) {
				$t = str_replace(array('<br />',"\n\n"), '', $rssItem['description']);
				$t = explode('<b>', $t);
				$V = array();
				$V['author'] = substr($rssItem['title'], 13);
				$V['pubDate']	= strtotime($rssItem['pubDate']);
				$V['description'] = trim($t[0]);
				
				$this->insertComment($iid, $i, $V['pubDate'], $V['author'], $V['description']);
				$i++;
			}
		}
		
		$this->query("
			UPDATE ideas
			SET queried=".time()."
			WHERE iid=$iid
			LIMIT 1");
	}


	private function insertIdea($status, $iid, $pubDate, $title, $description) {
		$title = addslashes($title);
		$description = addslashes($description);
		
		$this->query("
			INSERT INTO ideas (status, iid, pubDate, title, description)
			VALUES ('$status', $iid, $pubDate, '$title', '$description')
			ON DUPLICATE KEY UPDATE status='$status', pubDate=$pubDate, title='$title', description='$description'");
	}


	private function insertSolution($iid, $sid, $title, $description, $votes) {
		$title = addslashes($title);
		$description = addslashes($description);
		
		$this->query("
			INSERT INTO solutions (iid, sid, pubDate, title, description, votes)
			VALUES ($iid, $sid, ".time().", '$title', '$description', $votes)
			ON DUPLICATE KEY UPDATE title='$title', description='$description', votes='$votes'");
	}


	private function insertComment($iid, $cid, $pubDate, $author, $description) {
		$author = addslashes($author);
		$description = addslashes($description);
		
		$this->query("
			INSERT INTO comments (iid, cid, pubDate, author, description)
			VALUES ($iid, $cid, $pubDate, '$author', '$description')
			ON DUPLICATE KEY UPDATE pubDate=$pubDate, author='$author', description='$description'");
	}
	
	
	private function query($str) {
		
		$R = mysql_query($str, $this->db);
		
		if (!$R) {
			die('Invalid query: ' . mysql_error());
		}
		
		return $R;
	}
	
	
	private function get_https_rss($url) {
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
}
?>
