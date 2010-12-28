<?php

// This file should go in your ideatorrent server

$arr = array();
$id = array();

$dbh = pg_pconnect("host=localhost port=5432 dbname=pirata user=postgres password=pirata");

$R = pg_query($dbh,
  "SELECT c.status AS st, c.id AS id, c.date AS dt, c.title AS tt, c.description AS ds
  FROM qapoll_choice c
  ORDER BY c.id DESC
  LIMIT 12");

while ($r = pg_fetch_array($R, NULL, PGSQL_ASSOC)) {
  $arr[$r['st']][$r['id']] = $r;
  $id[] = $r['id'];
}

$R = pg_query($dbh, 
  "SELECT c.status AS st, c.id AS id, s.id AS sid, s.title AS tt, s.description AS ds, s.solution_votes AS vt, l.selected AS sl
  FROM qapoll_choice_solution s, qapoll_choice_solution_link l, qapoll_choice c
  WHERE c.id IN (".implode(',', $id).") AND l.choicesolutionid = s.id AND l.choiceid = c.id AND s.status=1
  ORDER BY s.id DESC
  LIMIT 1000");

while ($r = pg_fetch_array($R, NULL, PGSQL_ASSOC)) {
  $arr[$r['st']][$r['id']]['s'][$r['sid']] = $r;
}

//print_r($arr); // debugging

print(json_encode($arr));
?>
