<?php
session_start();
if(!isset($_SESSION['username'])){
    $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
    header('Location: ' . $home_url);
}
$username = $_SESSION['username'];
$id = $_GET['id'];
$data ='username='. $username . '&id='. $id;
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'http://gw127.iu.xsede.org:8000/query-api/make-public?'.$data);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_exec($ch);
$summary_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/summary.php?id=' . $id;
header('Location: ' . $summary_url);