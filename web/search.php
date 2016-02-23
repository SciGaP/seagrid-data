<?php
    session_start();
    if(!isset($_SESSION['username'])){
        $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }

    $q = "";
    $pageNo = 1;
    if(isset($_POST['query'])){
        $q = $_POST['query'];
    }
    if(isset($_POST['pageNo'])){
        $pageNo = $_POST['pageNo'];
    }
    if(isset($_POST['next'])){
        $pageNo = $pageNo + 1;
    }
    if(isset($_POST['previous'])){
        $pageNo = $pageNo - 1;
    }
    $offset = ($pageNo -1) * 50;
    $results = json_decode(file_get_contents('http://gw127.iu.xsede.org:8000/query-api/select?q='. urlencode($q)
        .'&limit=50&offset=' . $offset), true);
    if(!isset($results) || empty($results)){
        $results = array();
    }
?>

<html>
    <head>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="css/search.css">

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
                        <li class="active"><a href="./search.php">Search</a></li>
                    </ul>
                    <ul class="nav navbar-nav pull-right">
                        <li><a href="./logout.php">Logout</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">

            <div class="center-content">
                <form action="./search.php" method="post">
                    <div class="panel panel-default">
                        <div class="panel-body">
                            <div class="form-group search-text-block">
                                <input type="search" class="form-control" name="query" id="query"
                                       value="<?php if (isset($_POST['query'])) echo $_POST['query'] ?>">
                                <input type="hidden" name="pageNo" id="pageNo" value=<?php echo $pageNo; ?>>
                            </div>

                            <button type="submit" class="btn btn-primary pull-right" value="Search"><span
                                    class="glyphicon glyphicon-search"></span> Search
                            </button>

                        </div>
                    </div>
                    <hr>
                    <table class="table table-bordered">
                        <tr>
                            <th class="col-md-4">InChI</th>
                            <th class="col-md-3">Experiment Name</th>
                            <th class="col-md-2">Project Name</th>
                            <th class="col-md-3">Application</th>
                            <?php foreach ($results as $result): ?>
                        <tr>
                            <td><a href="./summary.php?id=<?php echo $result['ExperimentName']?>" target="_blank">
                                    <?php echo $result['Identifiers']['InChI']?></a></td>
                            <td><?php echo $result['ExperimentName']?></td>
                            <td><?php echo $result['ProjectName']?></td>
                            <td><?php echo $result['Calculation']['Package']?></td>
                        </tr>
                        <?php endforeach;?>
                        </tr>
                    </table>
                    <div class="pull-right btn-toolbar" style="padding-bottom: 5px">
                        <?php
                            if (isset($pageNo) && $pageNo != 1) {
                                echo '<input class="btn btn-primary btn-xs" type="submit" style="cursor: pointer" name="previous" value="previous"/>';
                            }
                            if (sizeof($results) > 0) {
                                echo '<input class="btn btn-primary btn-xs" type="submit" style="cursor: pointer" name="next" value="next"/>';
                            }
                        ?>
                    </div>
                </form>
            </div>

        </div><!-- /.container -->

        <!--JQuery MinJS-->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
    </body>
</html>
