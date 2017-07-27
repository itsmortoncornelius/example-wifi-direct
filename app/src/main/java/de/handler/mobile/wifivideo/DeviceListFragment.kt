package de.handler.mobile.wifivideo

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class DeviceListFragment : Fragment() {
	private var listener: OnListFragmentInteractionListener? = null
	private var recyclerView: RecyclerView? = null
	private var emptyList: TextView? = null


	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		return inflater!!.inflate(R.layout.fragment_device_list, container, false)
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		recyclerView = view?.findViewById(R.id.list)
		emptyList = view?.findViewById(R.id.list_empty)

		// Set the adapter
		recyclerView!!.layoutManager = LinearLayoutManager(recyclerView!!.context)
		recyclerView!!.adapter = WifiP2pDeviceRecyclerViewAdapter(listener)
	}


	override fun onAttach(context: Context?) {
		super.onAttach(context)
		if (context is OnListFragmentInteractionListener) {
			listener = context
		} else {
			throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
		}
	}

	override fun onDetach() {
		super.onDetach()
		listener = null
	}

	fun updateDevices(wifiP2pDevices: List<WifiP2pDevice>) {
		(recyclerView?.adapter as WifiP2pDeviceRecyclerViewAdapter).values = wifiP2pDevices
		if (wifiP2pDevices.isEmpty()) {
			recyclerView?.visibility = View.GONE
			emptyList?.visibility = View.VISIBLE
		} else {
			recyclerView?.visibility = View.VISIBLE
			emptyList?.visibility = View.GONE
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 *
	 *
	 * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
	 */
	interface OnListFragmentInteractionListener {
		fun onListFragmentInteraction(item: WifiP2pDevice)
	}

	companion object {
		fun newInstance(): Fragment {
			return DeviceListFragment()
		}
	}
}
