package kashyap.anurag.lmskotlin.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kashyap.anurag.lmskotlin.Fragments.*

class ViewPagerManegementAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm)  {

    override fun getCount(): Int {
        return 2;
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> {
                return AppliedFragment()
            }
            1 -> {
                return AllIssuedFragment()
            }
            else -> {
                return AppliedFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return "APPLIED"
            }
            1 -> {
                return "ISSUED"
            }
        }
        return super.getPageTitle(position)
    }
}