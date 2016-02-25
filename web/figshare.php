<?php
session_start();

if($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_SESSION['figshare-code'])){
    //publish the data to figshare
    $code = $_SESSION['figshare-code'];

    //FIXME - FigShare Side Issue
    $code = '9a60beb20f4c58bc84b28c4f6774b151a3a9a1f548821d94a7a950b734dc03f05bca3ff1395787b7f7aa4683910d1bb1bb7a64ea27a13761dd68c29a9cfd9985';

    $title = $_POST['title'];
    $description = $_POST['description'];
    $id = $_POST['id'];
    $tags = explode(' ',$_POST['tags']);
    $publishUrl = "https://api.figshare.com/v2/account/articles";
    $headers = array("Authorization: token " . $code);
    $data = json_encode(array('title'=>$title, 'description'=>$description, 'defined_type'=>'fileset', 'tags'=> $tags));
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $publishUrl);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    $temp = explode("/",json_decode($response)->location);
    $articleId = $temp[sizeof($temp)-1];
    $record = json_decode(file_get_contents(
        'http://gw127.iu.xsede.org:8000/query-api/get?id=' . $id), true);
    foreach($record['Files'] as $key=>$filePath){
        echo $filePath;
        $data = array($filePath, $articleId);
        shell_exec('python ./bin/figshare-workaround.py ' . escapeshellarg(json_encode($data)) . ' 2>&1');
    }

    $url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/summary.php?id=' . $id;
    header('Location: ' . $url);
}else{
    if (isset($_SESSION['username'])) {
        if(isset($_SESSION['figshare-code']) && isset($_GET['id'])){
            $showForm = true;
            $code = $_SESSION['figshare-code'];
            $id = $_GET['id'];
            $record = json_decode(file_get_contents(
                'http://gw127.iu.xsede.org:8000/query-api/get?id=' . $id), true);
        }else{
            if(!isset($_GET['code']) && isset($_GET['id'])){
                $key = "4ec77a6a3e1f6495690057fbca630441bca9a71b";
                $authorizeUrl = "https://figshare.com/account/applications/authorize";
                $redirectUrl = "https://gw127.iu.xsede.org/web/figshare.php";
                $url = $authorizeUrl . "?client_id=" . $key . "&response_type=code&scope=all&state=" . urlencode($_GET['id'])
                    . "&redirect_uri=" . $redirectUrl;
                header('Location: ' . $url);
            }else if(isset($_GET['code']) && isset($_GET['state'])){
                $showForm = true;
                $id = $_GET['state'];
                $code = $_GET['code'];
                $_SESSION['figshare-code'] = $code;
                $record = json_decode(file_get_contents(
                    'http://gw127.iu.xsede.org:8000/query-api/get?id=' . $id), true);
            }else{
                $home_url = 'https://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
                header('Location: ' . $home_url);
            }
        }
    } else {
        $home_url = 'https://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }
}
?>

<?php if(isset($showForm)):?>
<html>
<head>
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
          integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

    <!-- Optional theme -->
    <link rel="stylesheet" href="css/figshare.css">

</head>
<body>

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                    aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="./index.php">GridChem Data Catalog</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="./search.php">Search</a></li>
            </ul>
            <ul class="nav navbar-nav pull-right">
                <li class="active"><a href="./logout.php">Logout</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container">
    <form action="./figshare.php" method="post" role="form" class="form-fighsare">
        <input type="hidden" name="id" value="<?php echo $id?>">
        <div class="form-group required"><label class="control-label">Title</label>

            <div><input class="form-control" id="title" minlength="3" maxlength="100" name="title"
                        placeholder="title" required="required" type="text" value=""/>
            </div>
        </div>
        <div class="form-group required"><label class="control-label">Description</label>

            <div><textarea class="form-control" type="text" id="description" name="description" placeholder="description"
                        required="required" title="" rows="5"></textarea>
            </div>
        </div>
        <div class="form-group required"><label class="control-label">Tags&nbsp;<small>(space separated)</small></label>

            <div><input class="form-control" id="tags" name="tags"
                        placeholder="tags" required="required" title="" type="text"/>
            </div>
        </div>
        <div class="form-group required"><label class="control-label">File Set</label>
            <table class="table table-bordered">
                <?php if(isset($record['Files']['GaussianInputFile'])):?>
                    <tr>
                        <td>Gaussian Input File</td>
                        <td><a href=./download.php?file=<?php echo $record['Files']['GaussianInputFile']; ?>>
                                <?php echo basename($record['Files']['GaussianInputFile']); ?></a></td>
                    </tr>
                <?php endif; ?>
                <?php if(isset($record['Files']['GaussianOutputFile'])):?>
                    <tr>
                        <td>Gaussian Output File</td>
                        <td><a href=./download.php?file=<?php echo $record['Files']['GaussianOutputFile']; ?>>
                                <?php echo basename($record['Files']['GaussianOutputFile']); ?></a></td>
                    </tr>
                <?php endif; ?>
                <?php if(isset($record['Files']['GaussianCheckpointFile'])):?>
                    <tr>
                        <td>Gaussian Checkpoint File</td>
                        <td><a href=./download.php?file=<?php echo $record['Files']['GaussianCheckpointFile']; ?>>
                                <?php echo basename($record['Files']['GaussianCheckpointFile']); ?></a></td>
                    </tr>
                <?php endif; ?>
                <?php if(isset($record['Files']['GaussianFCheckpointFile'])):?>
                    <tr>
                        <td>Gaussian Formatted Checkpoint File</td>
                        <td><a href=./download.php?file=<?php echo $record['Files']['GaussianFCheckpointFile']; ?>>
                                <?php echo basename($record['Files']['GaussianFCheckpointFile']); ?></a></td>
                    </tr>
                <?php endif; ?>
            </table>
        </div>

        <br/>
        <input name="Submit" type="submit" class="btn btn-primary btn-block" value="Upload to FigShare">
    </form>
    <!--JQuery MinJS-->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
            integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
</body>
</html>
<?php endif;?>
