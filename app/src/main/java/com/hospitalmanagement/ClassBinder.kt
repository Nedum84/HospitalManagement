package com.hospitalmanagement


data class UserClassBinder(val user_id: String?,val user_card_no: String?, val name: String?, val username: String?,
                                      val phone: String?, val gender: String?, val age: String?, val address: String?,
                                      val pat_med_report: String?, val speciality: String?,val speciality_id: String?,
                                      val user_level: String?, val reg_date: String?, val photo_url: String?)

data class AppClassBinder(val app_id: Int?,val app_date: String, val pat_id: String, val pat_name: String,
                          val pat_symptoms: String?, var doc_prescription: String?, val doc_id: String?, val doc_name: String?
                          , var app_status: String?)//app status:: 0->pending, 1->cancelled,2-> done
data class DrugClassBinder(val drug_id: String?,val drug_name: String, val drug_desc: String, val drug_price: String, var drug_qty: Int=1)//drug qty for order quantities
data class MyOrderHistoryClassBinder(val order_id: String?,val order_no: String?, val order_price: String?, val order_drugs: String?, val pat_id: String?)//drug qty for order quantities

