import java.util.ArrayList;

class zad1 {
    public static void main(String[] args)
    {
        if(args.length < 3) {System.out.println("Not enough arguments"); return; }
        ArrayList<Integer> tab = new ArrayList<Integer>();
        for(String i:args)
        {
            try
            {
                tab.add(Integer.valueOf(Integer.parseInt(i)));
                Integer.valueOf(Integer.parseInt(i));
            }
            catch(NumberFormatException exc)
            { System.out.println("NumberFormatException"); return; }
        }

        if(tab.get(0) == 0)
        {
            if(tab.get(1) == 0)
            {
                if(tab.get(2) == 0)
                    System.out.println("Infinite amount of solutions, function equals 0");
                else
                    System.out.println("No solutions");
            }
            else
            {
                System.out.println("One solution: x0 = " + (-tab.get(2).intValue()/tab.get(1).intValue() ) );
            }
        }
        else
        {
            int delta = tab.get(1).intValue() * tab.get(1).intValue() - 4 * tab.get(0).intValue() * tab.get(2).intValue();
            if(delta < 0) System.out.println("No solutions in real numbers");
            else if(delta == 0)
            {
                System.out.printf("One solution: x0 = %.2f\n", (float)(-tab.get(1).intValue()/2/tab.get(0).intValue()) );
            }
            else
            {
                System.out.printf("Two solutions: x1 = %.2f; x2 = %.2f\n", (-tab.get(1).intValue() + java.lang.Math.sqrt(delta))/2/tab.get(0).intValue(),
                                    (-tab.get(1).intValue() - java.lang.Math.sqrt(delta))/2/tab.get(0).intValue());
            }
        }
    }
}