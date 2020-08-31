<?php
/**
 * Created by PhpStorm.
 * User: ray
 * Date: 12/06/2018
 * Time: 14:29
 */

class Bible_model extends CI_Model{
    public $status = 'error';
    public $message = 'Error processing requested operation.';
    public $user = "";

    function __construct(){
       parent::__construct();
	  }

    public function get_total_bible(){
      $this->db->select("COUNT(*) as num");
      $this->db->from('tbl_bible');
      $query = $this->db->get();
      $result = $query->row();
      if(isset($result)) return $result->num;
      return 0;
   }

   function fetchBible($book,$page=0){
       $this->db->select('tbl_bible.id, tbl_bible.book, tbl_bible.chapter, tbl_bible.verse, tbl_bible.'.$book);
       $this->db->from('tbl_bible');
       //$this->db->where('book',$book);
       if($page!=0){
           $this->db->limit(500,$page * 500);
       }else{
         $this->db->limit(500);
       }

       $query = $this->db->get();
       $result = $query->result();
       return $result;
   }
}
