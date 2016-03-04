<?php

include 'config.php';
session_start();

$file = $_GET['file'];
if ($file == null || (0 !== strpos($file, $_SESSION['username']))) {
    header('HTTP/1.0 403 Forbidden');
}

$file = DATA_ROOT . $file;

if (file_exists($file)) {
    header('Content-Description: File Transfer');
    header('Content-Type: application/octet-stream');
    header('Content-Disposition: attachment; filename="' . basename($file) . '"');
    header('Expires: 0');
    header('Cache-Control: must-revalidate');
    header('Pragma: public');
    header('Content-Length: ' . filesize($file));
    readfile($file);
    exit;
}
?>
