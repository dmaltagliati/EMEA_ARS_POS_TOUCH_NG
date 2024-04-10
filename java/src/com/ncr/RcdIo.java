package com.ncr;

import com.ncr.ecommerce.ECommerceManager;

/*******************************************************************
 *
 * Access to local parameter file S_PLURCD.DAT (binary search) (assuming all files to be sorted in ascending order) (assuming all
 * files to be named S_PLU???.DAT) (files are read-only and may or may not exist)
 *
 *******************************************************************/
class RcdIo extends BinIo {

	/***************************************************************************
	 * Constructor
	 *  @param id
	 *            String (3 chars) used as unique identification
	 * @param keySize
	 *            size of the record key in bytes
	 * @param recSize
	 ***************************************************************************/
	RcdIo(String id, int keySize, int recSize) {
		super(id, keySize, recSize);

		//ECOMMERCE-SSAM#A BEG
		if (!ECommerceManager.getInstance().isEnabled()) {
			open(null, "S_PLU" + id + ".DAT", 0);
		}
		//ECOMMERCE-SSAM#A END
	}
}

