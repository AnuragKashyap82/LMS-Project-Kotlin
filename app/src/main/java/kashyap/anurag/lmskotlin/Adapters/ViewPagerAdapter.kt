import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kashyap.anurag.lmskotlin.Fragments.HomeFragment
import kashyap.anurag.lmskotlin.Fragments.IssuedFragment
import kashyap.anurag.lmskotlin.Fragments.ReturnedFragment

class ViewPagerAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 3;
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> {
                return HomeFragment()
            }
            1 -> {
                return IssuedFragment()
            }
            2 -> {
                return ReturnedFragment()
            }
            else -> {
                return HomeFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0 -> {
                return "APPLIED"
            }
            1 -> {
                return "ISSUED"
            }
            2 -> {
                return "RETURNED"
            }
        }
        return super.getPageTitle(position)
    }

}
