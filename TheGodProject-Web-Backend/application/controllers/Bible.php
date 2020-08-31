<?php
defined('BASEPATH') OR exit('No direct script access allowed');

require APPPATH . '/libraries/BaseController.php';
/*
* This class handles some of the requests from the android client app
*/
class Bible extends BaseController {

	public function __construct()
    {
        parent::__construct();
    }


	//store user fcm token
	function get_bible(){
		$data = $this->get_data();
		$results = [];
		$isLastPage = false;
		$page = 0;
		if(isset($data->page)){
			$page = $data->page;
		}
		$book = "AMP";
		if(isset($data->book)){
			$book = $data->book;
		}
		$this->load->model('bible_model');
		$results = $this->bible_model->fetchBible($book,$page);
		$total_items = $this->bible_model->get_total_bible();
		$isLastPage = (($page + 1) * 500) >= $total_items;
		echo json_encode(array("status" => "ok","bible" => $results,"isLastPage" => $isLastPage,"next_page" => $page + 1));
	}


}
