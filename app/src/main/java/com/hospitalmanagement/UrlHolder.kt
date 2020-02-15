package com.hospitalmanagement

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat


object  UrlHolder{
        private const val URL_ROOT = "http://192.168.44.236/adsu/hospital-managent/server_request/"
//    private const val URL_ROOT = "http://unnelection.com.ng/adsu/hospital-managent/server_request/"



    val URL_GET_DRUGS: String?  = URL_ROOT + "get_drugs.php"
    val URL_CANCEL_APP: String?  = URL_ROOT + "cancel_app.php"
    val URL_GET_APPS: String?  = URL_ROOT + "get_apps.php"
    val URL_ADD_PRESCRIPTION: String?  = URL_ROOT + "add_prescription.php"
    val URL_ADD_APP: String?  = URL_ROOT + "add_app.php"
    val URL_LOGIN_REGISTER: String?  = URL_ROOT + "reg_log.php"
    val URL_ORDER_HISTORY: String?  = URL_ROOT + "get_orders.php"
    val URL_REMOVE_DOCTOR: String?  = URL_ROOT + "remove_doctor.php"
    val URL_MAKE_ORDER_PAYMENT: String?  = URL_ROOT + "add_order.php"
    val URL_MAKE_DOC_PHM: String?  = URL_ROOT + "reg_doc_phm.php"


    val allDrugs = "{\"error\":false,\"drugsz_arraysz\":[{\"drug_id\":\"1\",\"drug_name\":\"Paracetamol\",\"drug_desc\":\"\",\"drug_price\":\"100\"},{\"drug_id\":\"2\",\"drug_name\":\"Panadol\",\"drug_desc\":\"\",\"drug_price\":\"200\"},{\"drug_id\":\"3\",\"drug_name\":\"Panadol Extra\",\"drug_desc\":\"\",\"drug_price\":\"150\"},{\"drug_id\":\"4\",\"drug_name\":\"Vitamin C\",\"drug_desc\":\"\",\"drug_price\":\"50\"},{\"drug_id\":\"5\",\"drug_name\":\"Procold\",\"drug_desc\":\"\",\"drug_price\":\"150\"},{\"drug_id\":\"6\",\"drug_name\":\"Vicodin \",\"drug_desc\":\"Vicodin is a popular drug for treating acute or chronic moderate to moderately severe pain. Its most common side effects are lightheadedness, dizziness, sedation, nausea, and vomiting. Vicodin can reduce breathing, impair thinking, reduce physical abilities, and is habit forming.\",\"drug_price\":\"50\"},{\"drug_id\":\"7\",\"drug_name\":\"Simvastatin \",\"drug_desc\":\"Simvastatin is one of the first \\\"statins\\\" (HMG-CoA reductase inhibitors) approved for treating high cholesterol and reducing the risk of stroke, death from heart disease, and risk of heart attacks. Its most common side effects are headache, nausea, vomiting, diarrhea, abdominal pain, and muscle pain. Like other statins it can cause muscle break down. \",\"drug_price\":\"500\"},{\"drug_id\":\"8\",\"drug_name\":\"Lisinopril \",\"drug_desc\":\"Lisinopril is an angiotensin converting enzyme (ACE) inhibitor used for treating high blood pressure, congestive heart failure, and for preventing kidney failure caused by high blood pressure and diabetes. Lisinopril side effects include dizziness, nausea, headaches, drowsiness, and sexual dysfunction. ACE inhibitors may cause a dry cough that resolves when the drug is discontinued. \",\"drug_price\":\"800\"},{\"drug_id\":\"9\",\"drug_name\":\"Levothyroxine \",\"drug_desc\":\"Levothryoxine is a man-made version of thyroid hormone. It is used for treating hypothyroidism. Its side effects are usually result from high levels of thyroid hormone. Excessive thyroid hormone can cause chest pain, increased heart rate, excessive sweating, heat intolerance, nervousness, headache, and weight loss.\",\"drug_price\":\"250\"},{\"drug_id\":\"10\",\"drug_name\":\"Azithromycin \",\"drug_desc\":\"Azithromycin is an antibiotic used for treating ear, throat, and sinus infections as well as pneumonia, bronchitis, and some sexually transmitted diseases. Its common side effects include loose stools, nausea, stomach pain, and vomiting. Rare side effects include abnormal liver tests, allergic reactions, nervousness, and abnormal heart beats. \",\"drug_price\":\"120\"},{\"drug_id\":\"11\",\"drug_name\":\"Metformin \",\"drug_desc\":\"Metformin is used alone or in combination with other drugs for treating type 2 diabetes in adults and children. The most common side effects of metformin are nausea, vomiting, gas, bloating, diarrhea, and loss of appetite.\",\"drug_price\":\"700\"},{\"drug_id\":\"12\",\"drug_name\":\"Lipitor\",\"drug_desc\":\"Lipitor is a \\\"statin\\\" (HMG-CoA reductase inhibitors) approved for treating high cholesterol. It also prevents chest pain, stroke, heart attack in individuals with coronary artery disease. It causes minor side effects such as constipation, diarrhea, fatigue, gas, heartburn, and headache. Like other statins it can cause muscle pain and muscle break down. \",\"drug_price\":\"1500\"},{\"drug_id\":\"13\",\"drug_name\":\"Amlodipine\",\"drug_desc\":\"Amlodipine is a calcium channel blocker used for treating high blood pressure and for treatment and prevention of chest pain. Its most common side effects are headache and swelling of the lower extremities. Amlodipine can also cause dizziness, flushing, fatigue, nausea, and palpitations. \",\"drug_price\":\"400\"},{\"drug_id\":\"14\",\"drug_name\":\"Amoxicillin\",\"drug_desc\":\"Amoxicillin is a penicillin type antibiotic used for treating several types of bacterial infections such as ear, tonsils, throat, larynx, urinary tract, and skin infections. Its side effects are diarrhea, heartburn, nausea, itching, vomiting, confusion, abdominal pain, rash, and allergic reactions.\",\"drug_price\":\"600\"},{\"drug_id\":\"15\",\"drug_name\":\"Hydrochlorothiazide\",\"drug_desc\":\"Hydrochlorothiazide is a diuretic (water pill) used alone or combined with other drugs for treating high blood pressure. Its side effects include weakness, low blood pressure, light sensitivity, impotence, nausea, abdominal pain, electrolyte disturbances, and rash.\",\"drug_price\":\"2500\"}]}"
}