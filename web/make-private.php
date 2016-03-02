<?php
session_start();
if(!isset($_SESSION['username'])){
    $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
    header('Location: ' . $home_url);
}
$username = $_SESSION['username'];
$id = $_GET['id'];
$data ='username='. $username . '&id='. $id;
$record = json_decode(file_get_contents(
    'http://gw127.iu.xsede.org:8000/query-api/make-private?'.$data), true);
if($record != null) {
    $summary_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/summary.php?id=' . $id;
    header('Location: ' . $summary_url);
}else{
    echo '<h2>Failed Operation!</h2>';
}