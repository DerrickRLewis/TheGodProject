<section class="content">
    <!-- Page content-->
    <div class="container-fluid">
        <div class="block-header">
            <h2>Edit Church Branch</h2>
        </div>


            <div class="row clearfix">
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <div class="card">

                        <div class="body">
                          <div class="card-inner">
                          <form method="POST" action="<?php echo base_url(); ?>editBranchData" style="margin-top:30px;">
                              <input type="hidden" class="form-control" name="id" placeholder="Branch Name" required="" autofocus="" value="<?php echo $branch->id; ?>">
                            <div class="input-group addon-line" style="margin-top:20px;">

                                <div class="form-line">
                                    <input type="text" class="form-control" name="name" placeholder="Branch Name" required="" autofocus="" value="<?php echo $branch->name; ?>">
                                </div>
                            </div>

                            <div class="input-group addon-line" style="margin-top:20px;">

                                <div class="form-line">
                                    <input type="text" class="form-control" name="address" placeholder="Branch Address" required="" autofocus="" value="<?php echo $branch->address; ?>">
                                </div>
                            </div>

                            <div class="input-group addon-line" style="margin-top:20px;">

                                <div class="form-line">
                                    <input type="text" class="form-control" name="pastor" placeholder="Branch Pastor" autofocus="" value="<?php echo $branch->pastor; ?>">
                                </div>
                            </div>

                            <div class="input-group addon-line" style="margin-top:20px;">

                                <div class="form-line">
                                    <input type="text" class="form-control" name="phone" placeholder="Branch Contact Phone" autofocus="" value="<?php echo $branch->phone; ?>">
                                </div>
                            </div>

                            <div class="input-group addon-line" style="margin-top:20px;">

                                <div class="form-line">
                                    <input type="text" class="form-control" name="email" placeholder="Branch Contact Email" autofocus="" value="<?php echo $branch->email; ?>">
                                </div>
                            </div>

                            <div class="input-group addon-line" style="margin-top:20px; display:none;">

                                <div class="form-line">
                                    <input type="number" step="any" class="form-control" name="latitude" placeholder="Branch Latitude" autofocus="" value="<?php echo $branch->latitude; ?>">
                                </div>
                            </div>

                            <div class="input-group addon-line" style="margin-top:20px; display:none;">

                                <div class="form-line">
                                    <input type="number" step="any" class="form-control" name="longitude" placeholder="Branch Longitude" autofocus="" value="<?php echo $branch->longitude; ?>">
                                </div>
                            </div>

                             <?php $this->load->helper('form'); ?>
                             <div class="row">
                                 <div class="col-md-12">
                                     <?php echo validation_errors('<div class="alert alert-danger alert-dismissable">', ' <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button></div>'); ?>
                                 </div>
                             </div>
                             <?php
                             $this->load->helper('form');
                             $error = $this->session->flashdata('error');
                             if($error)
                             {
                                 ?>
                                 <div class="alert alert-danger alert-dismissable">
                                     <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                                     <?php echo $error; ?>
                                 </div>
                             <?php }
                             $success = $this->session->flashdata('success');
                             if($success)
                             {
                                 ?>
                                 <div class="alert alert-success alert-dismissable">
                                     <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                                     <?php echo $success; ?>
                                 </div>
                             <?php } ?>

                            <div class="box-footer text-center">
                               <button class="btn btn-primary waves-effect" type="submit">UPDATE BRANCH</button>
                            </div>

                          </form>
                        </div>
                      </div>
                    </div>
                </div>
    </div>
</section>
