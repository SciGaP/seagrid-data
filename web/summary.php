<?php
    session_start();
    if(!isset($_SESSION['username'])){
        $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }

    $id = $_GET['id'];
    if(isset($id)){
        $record = json_decode(file_get_contents(
            'http://gw127.iu.xsede.org:8000/query-api/get?id=' . $id), true);
    }else{
        echo 'Id not set !!!';
    }
?>

<?php if(isset($record)): ?>
<html>
    <head>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="css/summary.css">


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
                        <li><a href="./logout.php">Logout</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <div class="center-content">
                <div style="color: red">&nbsp;&nbsp;&nbsp;N.B: This data is automatically extracted using set of configured parser
                    and may contain errors. Please report any issues in the <a href="https://issues.apache.org/jira/browse/AIRAVATA/?
                    selectedTab=com.atlassian.jira.jira-projects-plugin:summary-panel" target="_blank">issue tracker</a></div>
                <div class="col-md-8 text-centered">
                    <hr>
                    <table class="table table-bordered">
                        <tr><td><h4>Identifiers</h4></td><td></td></tr>
                        <?php if(isset($record['Identifiers']['InChI'])):?>
                        <tr>
                            <td>InChI</td>
                            <td><?php echo $record['Identifiers']['InChI']?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Identifiers']['InChIKey'])):?>
                            <tr>
                                <td>InChI Key</td>
                                <td><?php echo $record['Identifiers']['InChIKey']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Identifiers']['SMILES'])):?>
                            <tr>
                                <td>SMILES</td>
                                <td><?php echo $record['Identifiers']['SMILES']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Identifiers']['CanonicalSMILES'])):?>
                            <tr>
                                <td>Canonical SMILES</td>
                                <td><?php echo $record['Identifiers']['CanonicalSMILES']?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Organization</h4></td><td></td></tr>
                        <?php if(isset($record['ExperimentName'])):?>
                        <tr>
                            <td>Experiment</td>
                            <td><?php echo $record['ExperimentName']?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($record['ProjectName'])):?>
                            <tr>
                                <td>Project</td>
                                <td><?php echo $record['ProjectName']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Username'])):?>
                            <tr>
                                <td>Owner</td>
                                <td><?php echo $record['Username']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['IndexedTime'])):?>
                            <tr>
                                <td>Indexed Time</td>
                                <td>
                                    <?php
                                        $date = new DateTime();
                                        $date->setTimestamp($record['IndexedTime']);
                                        echo $date->format('U = Y-m-d H:i:s')
                                    ?>
                                </td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Calculation</h4></td><td></td></tr>
                        <?php if(isset($record['Calculation']['Package'])):?>
                        <tr>
                            <td>Package</td>
                            <td><?php echo $record['Calculation']['Package'] ?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Calculation']['CalcType'])):?>
                        <tr>
                            <td>Calculation Type</td>
                            <td><?php echo $record['Calculation']['CalcType'] ?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Calculation']['Basis'])):?>
                            <tr>
                                <td>Basis</td>
                                <td><?php echo $record['Calculation']['Basis'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Calculation']['NBasis'])):?>
                            <tr>
                                <td>Number of Basis Functions</td>
                                <td><?php echo $record['Calculation']['NBasis'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Calculation']['Keywords'])):?>
                            <tr>
                                <td>Keywords</td>
                                <td><?php echo $record['Calculation']['Keywords'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Calculation']['JobStatus'])):?>
                            <tr>
                                <td>Job Status</td>
                                <td><?php echo $record['Calculation']['JobStatus'] ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Molecule</h4></td><td></td></tr>
                        <?php if(isset($record['Molecule']['Formula'])):?>
                            <tr>
                                <td>Formula</td>
                                <td><?php echo $record['Molecule']['Formula'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Molecule']['NAtom'])):?>
                            <tr>
                                <td>Number of Atoms</td>
                                <td><?php echo $record['Molecule']['NAtom'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Molecule']['NMo'])):?>
                            <tr>
                                <td>Molecular Mass</td>
                                <td><?php echo $record['Molecule']['NMo'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Molecule']['ElecSym'])):?>
                            <tr>
                                <td>Electron Symmetry</td>
                                <td><?php echo $record['Molecule']['ElecSym'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Molecule']['Multiplicity'])):?>
                            <tr>
                                <td>Multiplicity</td>
                                <td><?php echo $record['Molecule']['Multiplicity'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Molecule']['Charge'])):?>
                            <tr>
                                <td>Charge</td>
                                <td><?php echo $record['Molecule']['Charge'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Molecule']['OrbSym'])):?>
                            <tr>
                                <td>Orbital Symmetry</td>
                                <td><?php echo $record['Molecule']['OrbSym'] ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Calculated Properties</h4></td><td></td></tr>
                        <?php if(isset($record['CalculatedProperties']['Energy'])):?>
                            <tr>
                                <td>Energy</td>
                                <td><?php echo $record['CalculatedProperties']['Energy'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['CalculatedProperties']['Dipole'])):?>
                            <tr>
                                <td>Dipole</td>
                                <td><?php echo $record['CalculatedProperties']['Dipole'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['CalculatedProperties']['HF'])):?>
                            <tr>
                                <td>HF</td>
                                <td><?php echo $record['CalculatedProperties']['HF'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['CalculatedProperties']['Homos'])):?>
                            <tr>
                                <td>Homos</td>
                                <td><?php echo json_encode($record['CalculatedProperties']['Homos']); ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Execution Environment</h4></td></tr>
                        <?php if(isset($record['ExecutionEnvironment']['CalcBy'])):?>
                            <tr>
                                <td>Calculated By</td>
                                <td><?php echo $record['ExecutionEnvironment']['CalcBy']; ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['ExecutionEnvironment']['CalcMachine'])):?>
                            <tr>
                                <td>Calculated Machine</td>
                                <td><?php echo $record['ExecutionEnvironment']['CalcMachine']; ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['ExecutionEnvironment']['FinTime'])):?>
                            <tr>
                                <td>Finished Time</td>
                                <td><?php echo $record['ExecutionEnvironment']['FinTime']; ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>File Set</h4></td></tr>
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
                        <?php if(isset($record['Files']['SDFStructureFile'])):?>
                            <tr>
                                <td>SDF Structure File</td>
                                <td><a href=./download.php?file=<?php echo $record['Files']['SDFStructureFile']; ?>>
                                        <?php echo basename($record['Files']['SDFStructureFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Files']['PDBStructureFile'])):?>
                            <tr>
                                <td>PDB Structure File</td>
                                <td><a href=./download.php?file=<?php echo $record['Files']['PDBStructureFile']; ?>>
                                        <?php echo basename($record['Files']['PDBStructureFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Files']['InChIFile'])):?>
                            <tr>
                                <td>PDB Structure File</td>
                                <td><a href=./download.php?file=<?php echo $record['Files']['InChIFile']; ?>>
                                        <?php echo basename($record['Files']['InChIFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($record['Files']['SMILESFile'])):?>
                            <tr>
                                <td>SMILES File</td>
                                <td><a href=./download.php?file=<?php echo $record['Files']['SMILESFile']; ?>>
                                        <?php echo basename($record['Files']['SMILESFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                    </table>
                    <div class="btn-toolbar">
                        <a href="./figshare.php?id=<?php echo $record['ExperimentName']?>" target="_self"
                           class="btn btn-primary"
                           role="button"
                           title="Publish the data and files to figshare" target="_blank">Upload Files to FigShare
                        </a>
                    </div>
                    <br>
                </div>
                <div class="col-md-4">
                    <hr>
                    <div id="glmol01" style="width: 300px; height: 300px; background-color: black;margin-left: 10%"></div>
                    <textarea id="glmol01_src" style="display: none;">
                        <?php var_dump($record['FinalMoleculeStructuralFormats']['SDF'])?>
                    </textarea>
                    <div class="text-centered">Molecular Structure</div>
                    <br><br>
                    <?php if(isset($record['CalculatedProperties']['EnergyDistribution'])):?>
                        <canvas id="energyDistribution" width="300" height="300" style="margin-left: 10%"></canvas>
                        <div class="text-centered">Energy vs Iteration</div>
                    <?php endif; ?>
                    <?php if(isset($record['CalculatedProperties']['MaximumGradientDistribution'])):?>
                        <br><br>
                        <canvas id="gradientDistribution" width="300" height="300" style="margin-left: 10%"></canvas>
                        <div class="text-centered">Gradient vs Iteration</div>
                    <?php endif; ?>
                </div>
            </div>

        </div><!-- /.container -->

        <!--JSMol-->
        <script src="./js/Three.js"></script>
        <script src="./js/GLmol.js"></script>
        <!--JQuery MinJS-->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
        <!--Charting library-->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/1.0.2/Chart.min.js"></script>
    <script>
        $( document ).ready(function() {
            var glmol01 = new GLmol('glmol01', true);

            glmol01.defineRepresentation = function() {
                var all = this.getAllAtoms();
                var hetatm = this.removeSolvents(this.getHetatms(all));
                this.colorByAtom(all, {});
                this.colorByChain(all);
                var asu = new THREE.Object3D();
                this.drawBondsAsStick(asu, hetatm, this.cylinderRadius, this.cylinderRadius);
                this.drawBondsAsStick(asu, this.getResiduesById(this.getSidechains(this.getChain(all, ['A'])), [58, 87]),
                    this.cylinderRadius, this.cylinderRadius);
                this.drawBondsAsStick(asu, this.getResiduesById(this.getSidechains(this.getChain(all, ['B'])), [63, 92]),
                    this.cylinderRadius, this.cylinderRadius);
                this.drawCartoon(asu, all, this.curveWidth, this.thickness);
                this.drawSymmetryMates2(this.modelGroup, asu, this.protein.biomtMatrices);
                this.modelGroup.add(asu);
            };

            glmol01.loadMolecule();
        });
    </script>
        <script>
            $( document ).ready(function() {
                <?php if(isset($record['CalculatedProperties']['MaximumGradientDistribution'])):?>
                var gradientData = {
                    labels: <?php
                                if(sizeof($record['CalculatedProperties']['Iterations']) > 20){
                                    $step = sizeof($record['CalculatedProperties']['Iterations'])/20;
                                    echo "[" . $record['CalculatedProperties']['Iterations'][0];
                                    $i = $step;
                                    while($i < sizeof($record['CalculatedProperties']['Iterations'])){
                                        echo "," . $record['CalculatedProperties']['Iterations'][round($i)];
                                        $i = $i + $step;
                                    }
                                    echo "]";
                                }else{
                                    echo json_encode($record['CalculatedProperties']['Iterations']);
                                }
                            ?>,
                    datasets: [
                        {
                            label: "Maximum Gradient",
                            fillColor: "rgba(220,220,220,0.2)",
                            strokeColor: "rgba(220,220,220,1)",
                            pointColor: "rgba(220,220,220,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(220,220,220,1)",
                            data: <?php
                                        if(sizeof($record['CalculatedProperties']['MaximumGradientDistribution']) > 20){
                                            $step = sizeof($record['CalculatedProperties']['MaximumGradientDistribution'])/20;
                                            echo "[" . $record['CalculatedProperties']['MaximumGradientDistribution'][0];
                                            $i = $step;
                                            while($i < sizeof($record['CalculatedProperties']['MaximumGradientDistribution'])){
                                                echo "," . $record['CalculatedProperties']['MaximumGradientDistribution'][round($i)];
                                                $i = $i + $step;
                                            }
                                            echo "]";
                                        }else{
                                            echo json_encode($record['CalculatedProperties']['MaximumGradientDistribution']);
                                        }
                                    ?>
                        },
                        {
                            label: "RMS Gradient",
                            fillColor: "rgba(151,187,205,0.2)",
                            strokeColor: "rgba(151,187,205,1)",
                            pointColor: "rgba(151,187,205,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(151,187,205,1)",
                            data: <?php
                                        if(sizeof($record['CalculatedProperties']['RMSGradientDistribution']) > 20){
                                            $step = sizeof($record['CalculatedProperties']['RMSGradientDistribution'])/20;
                                            echo "[" . $record['CalculatedProperties']['RMSGradientDistribution'][0];
                                            $i = $step;
                                            while($i < sizeof($record['CalculatedProperties']['RMSGradientDistribution'])){
                                                echo "," . $record['CalculatedProperties']['RMSGradientDistribution'][round($i)];
                                                $i = $i + $step;
                                            }
                                            echo "]";
                                        }else{
                                            echo json_encode($record['CalculatedProperties']['RMSGradientDistribution']);
                                        }
                                    ?>
                        }
                    ]
                };
                var ctx1 = document.getElementById("gradientDistribution").getContext("2d");
                var options1 = {
                    legendTemplate : '<ul>'
                    +'<% for (var i=0; i<datasets.length; i++) { %>'
                    +'<li>'
                    +'<span style=\"background-color:<%=datasets[i].lineColor%>\"></span>'
                    +'<% if (datasets[i].label) { %><%= datasets[i].label %><% } %>'
                    +'</li>'
                    +'<% } %>'
                    +'</ul>'
                }
                var gradChart = new Chart(ctx1, options1).Line(gradientData, {multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"});
                var gradLegend = gradChart.generateLegend();
                $('#gradientDistribution').append(gradLegend);
                <?php endif; ?>

                var energyData = {
                    labels: <?php
                                if(sizeof($record['CalculatedProperties']['Iterations']) > 20){
                                    $step = sizeof($record['CalculatedProperties']['Iterations'])/20;
                                    echo "[" . $record['CalculatedProperties']['Iterations'][0];
                                    $i = $step;
                                    while(round($i) < sizeof($record['CalculatedProperties']['Iterations'])){
                                        echo "," . $record['CalculatedProperties']['Iterations'][round($i)];
                                        $i = $i + $step;
                                    }
                                    echo "]";
                                }else{
                                    echo json_encode($record['CalculatedProperties']['Iterations']);
                                }
                            ?>,
                    datasets: [
                        {
                            label: "Energy Distribution",
                            fillColor: "rgba(220,220,220,0.2)",
                            strokeColor: "rgba(220,220,220,1)",
                            pointColor: "rgba(220,220,220,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(220,220,220,1)",
                            data: <?php
                                        if(sizeof($record['CalculatedProperties']['EnergyDistribution']) > 20){
                                            $step = sizeof($record['CalculatedProperties']['EnergyDistribution'])/20;
                                            echo "[" . $record['CalculatedProperties']['EnergyDistribution'][0];
                                            $i = $step;
                                            while(round($i) < sizeof($record['CalculatedProperties']['EnergyDistribution'])){
                                                echo "," . $record['CalculatedProperties']['EnergyDistribution'][round($i)];
                                                $i = $i + $step;
                                            }
                                            echo "]";
                                        }else{
                                            echo json_encode($record['CalculatedProperties']['EnergyDistribution']);
                                        }
                                    ?>
                        }
                    ]
                };
                <?php if(isset($record['CalculatedProperties']['EnergyDistribution'])):?>
                var ctx2 = document.getElementById("energyDistribution").getContext("2d");
                var options2 = {
                    legendTemplate : '<ul>'
                    +'<% for (var i=0; i<datasets.length; i++) { %>'
                    +'<li>'
                    +'<span style=\"background-color:<%=datasets[i].lineColor%>\"></span>'
                    +'<% if (datasets[i].label) { %><%= datasets[i].label %><% } %>'
                    +'</li>'
                    +'<% } %>'
                    +'</ul>'
                }
                var energyChart = new Chart(ctx2, options2).Line(energyData, {multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"});
                var energyLegend = energyChart.generateLegend();
                $('#energyDistribution').append(energyLegend);
                <?php endif; ?>
            });
        </script>
    </body>
</html>
<?php endif; ?>
