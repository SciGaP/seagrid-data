<?php
    include 'config.php';
    //FIXME Time should be shown in locally
    date_default_timezone_set('America/Indianapolis');

    function convert_date($hit)
    {
        $hit[0] = substr($hit[0],1,-1);
        return intval(strtotime($hit[0])*1000);
    }

    session_start();
    if(!isset($_SESSION['username'])){
        $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }

    $q = "";
    $pageNo = 1;
    if(isset($_POST['query'])){
        $q = $_POST['query'];
        //converting time to unix timestamp
        $q = preg_replace_callback('~("\\d{4}/\\d{2}/\\d{2}")~', convert_date, $q);
    }
    if(!isset($_POST['search']) && isset($_POST['pageNo'])){
        $pageNo = $_POST['pageNo'];
    }
    if(!isset($_POST['search']) && isset($_POST['next'])){
        $pageNo = $pageNo + 1;
    }
    if(!isset($_POST['search']) && isset($_POST['previous'])){
        $pageNo = $pageNo - 1;
    }
    $offset = ($pageNo -1) * 50;
    $username = $_SESSION['username'];
    $results = json_decode(file_get_contents('http://gw127.iu.xsede.org:8000/query-api/select?username='.$username.'&q='. urlencode($q)
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
        <link rel="stylesheet" href="assets/css/search.css">
        <!-- Query builder -->
        <link rel="stylesheet" href="assets/css/query-builder.default.min.css">
        <link rel="stylesheet" href="assets/css/bootstrap-datepicker.css">

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
                    <a class="navbar-brand" href="./index.php">SEAGrid Data Catalog</a>
                </div>
                <div id="navbar" class="collapse navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="active"><a href="./search.php">Search</a></li>
                        <li><a href="./browse.php">Directory Browser</a></li>
                    </ul>
                    <ul class="nav navbar-nav pull-right">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown"><?php echo $_SESSION['username'] ?><span class="caret"></span></a>
                            <ul class="dropdown-menu" role="menu">
                                <li><a href="./logout.php"><span class="glyphicon glyphicon-log-out"></span> Log out</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <div class="center-content">
                <div id="builder"></div>
                <div class="btn-group pull-right">
                    <button id="btn-reset" class="btn btn-warning reset">Reset</button>
                    <button id="btn-search" class="btn btn-primary parse-json">Search</button>
                </div>
                <form id="searchForm" action="./search.php" method="post">
                    <div class="form-group search-text-block">
                        <input id="query" name="query" type="hidden" value='<?php if (isset($_POST['query'])) echo $_POST['query'] ?>'>
                        <input type="hidden" name="pageNo" id="pageNo" value=<?php echo $pageNo; ?>>
                    </div>
                    <br>
                    <hr>
                    <table class="table table-bordered">
                        <tr>
                            <th class="col-md-2">Experiment Name</th>
                            <th class="col-md-2">Project Name</th>
                            <th class="col-md-2">Package</th>
                            <th class="col-md-2">Formula</th>
                            <th class="col-md-2">Indexed Time</th>
                            <?php foreach ($results as $result): ?>
                        <tr>
                            <td><a href="./summary.php?id=<?php echo $result['Id']?>" target="_blank">
                                    <?php echo $result['ExperimentName']?></a></td>
                            <td><?php echo $result['ProjectName']?></td>
                            <td><?php echo $result['Calculation']['Package']?></td>
                            <td><?php echo $result['Molecule']['Formula']?></td>
                            <td>
                                <?php
                                    $date = new DateTime();
                                    $date->setTimestamp($result['IndexedTime']/1000);
                                    echo $date->format('Y-m-d H:i:s')
                                ?>
                            </td>
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
        <!-- Query builder-->
        <script src="assets/js/query-builder.standalone.js"></script>
        <script src="assets/js/moment.min.js"></script>
        <script src="assets/js/bootstrap-datepicker.js"></script>

        <script>
            $( document ).ready(function() {
                var rules_basic = {
                    condition: 'AND',
                    rules: [{
                        id: 'ExperimentName',
                        operator: 'contains',
                        value: ''
                    }]
                };

                $('#builder').queryBuilder({
                    plugins: ['bt-tooltip-errors'],

                    filters: [{
                        id: 'ExperimentName',
                        label: 'Experiment Name',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'ProjectName',
                        label: 'Project Name',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'Calculation.Package',
                        label: 'Package',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'Molecule.Formula',
                        label: 'Formula',
                        type: 'string',
                        operators: ['contains', 'equal']
                    }, {
                        id: 'Identifiers.InChI',
                        label: 'InChI',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'Identifiers.SMILES',
                        label: 'SMILES',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Calculation.CalcType',
                        label: 'Calculation Type',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Calculation.Methods',
                        label: 'Calculation Methods',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Calculation.Basis',
                        label: 'Basis Sets',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Molecule.NAtom',
                        label: 'Number of Atoms',
                        type: 'integer',
                        operators: ['equal', 'less', 'greater']
                    },{
                        id: 'ExecutionEnvironment.ActualJobRunTime',
                        label: 'Actual Job Run Time',
                        type: 'integer',
                        operators: ['equal', 'less', 'greater']
                    }, {
                        id: 'IndexedTime',
                        label: 'Indexed Time',
                        type: 'date',
                        operators: ['between'],
                        validation: {
                            format: 'YYYY/MM/DD'
                        },
                        plugin: 'datepicker',
                        plugin_config: {
                            format: 'yyyy/mm/dd',
                            todayBtn: 'linked',
                            todayHighlight: true,
                            autoclose: true
                        }
                    }],

                    rules: rules_basic
                });

                $('#btn-reset').on('click', function() {
                    $('#builder').queryBuilder('reset');
                    $('#builder').queryBuilder('setRules', rules_basic);
                });

                $('#btn-search').on('click', function() {
                    var result = $('#builder').queryBuilder('getMongo');
                    if(JSON.stringify(result, null, 2) != "{}"){
                        $('#query').val(JSON.stringify(result, null, 2));
                        $('#pageNo').val(1);
                        $('form#searchForm').submit();
                    }
                });

                if($('#query').val() != ""){
                    var result = $('#query').val();
                    $('#builder').queryBuilder('setRulesFromMongo', $.parseJSON(result));
                }
            });
        </script>
    </body>
</html>
