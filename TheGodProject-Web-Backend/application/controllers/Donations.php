<?php
defined('BASEPATH') OR exit('No direct script access allowed');

require APPPATH . '/libraries/BaseController.php';

class Donations extends BaseController {

	public function __construct()
    {
        parent::__construct();
				$this->isLoggedIn();
				$this->load->model('donations_model');
    }

		public function index(){
        $this->load->template('donations', []); // this will load the view file
    }


		function donationslisting(){
			// Datatables Variables

				$draw = intval($_POST['draw']);
				$start = intval($_POST['start']);
				$length = intval($_POST['length']);
				$columnIndex = $_POST['order'][0]['column']; // Column index
				$columnName = $_POST['columns'][$columnIndex]['data']; // Column name
				$columnSortOrder = $_POST['order'][0]['dir']; // asc or desc
				$searchValue="";
				if(isset($_POST['search']['value'])){
					$searchValue = $_POST['search']['value']; // Search value
				}

				$columnName="";
				if(isset($_POST['columns'][$columnIndex]['data'])){
					$columnSortOrder = $_POST['columns'][$columnIndex]['data']; // Search value
				}

				$columnSortOrder = "ASC";
				if(isset($_POST['order'][0]['dir'])){
					$columnSortOrder = $_POST['order'][0]['dir']; // Search value
				}


				$users = $this->donations_model->donationsListing($columnName,$columnSortOrder,$searchValue,$start, $length);
				$total_users = $this->donations_model->get_total_donations($searchValue);
				//var_dump($users); die;
				$dat = array();

				 $count = $start + 1;
				foreach($users as $r) {
						 $dat[] = array(
									$count,
									$r->reason,
									$r->email,
									$r->name,
									$r->reference,
									$r->amount,
									$r->method,
									$r->date
						 );
						 $count++;

				}

				$output = array(
						 "draw" => $draw,
							 "recordsTotal" => $total_users,
							 "recordsFiltered" => $total_users,
							 "data" => $dat
					);
				echo json_encode($output);
		}
}
