<?php
include 'config.php';
include './lib/FileManager.php';

session_start();
date_default_timezone_set('America/Indianapolis');

if(!isset($_SESSION['username']) || !(strtolower(filter_input(INPUT_SERVER, 'HTTP_X_REQUESTED_WITH')) === 'xmlhttprequest')){
    $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
    header('Location: ' . $home_url);
}

$path = $_GET['path'];
if( $path == null || (0 !== strpos($path, $_SESSION['username']))){
    header('HTTP/1.0 403 Forbidden');
}

if (!file_exists(DATA_ROOT . $path))
    echo FileManager::msg(False, "$path does not exist");

if (is_dir(DATA_ROOT . $path))
    echo FileManager::get_content(DATA_ROOT, $path);
else
    echo file_get_contents(DATA_ROOT . $path);
