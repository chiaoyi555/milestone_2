
public enum Register{
        // all the register
        ZERO("$zero", 0),
        V0("$v0", 2), V1("$v1", 3),
        A0("$a0", 4), A1("$a1", 5), A2("$a2", 6), A3("$a3", 7),
        T0("$t0", 8), T1("$t1", 9), T2("$t2", 10), T3("$t3", 11), T4("$t4", 12), 
        T5("$t5", 13), T6("$t6", 14), T7("$t7", 15), T8("$t8", 24), T9("$t9", 25),
        S0("$s0", 16), S1("$s1", 17), S2("$s2", 18), S3("$s3", 19), S4("$s4", 20), 
        S5("$s5", 21), S6("$s6", 22),S7("$s7", 23),
        SP("$sp", 29),
        FP("$fp", 30),
        RA("$ra", 31),
        GP("$gp", 28),
        AT("$at", 1),
        K0("$k0", 26),
        K1("$k1", 27);

        private final String name;
        private final int number;

        private Register(String name, int number){
            this.name = name;
            this.number = number;
        }

        public int getnumber() {
            return number;
        }

        public String getName() {
            return name;
        }

        // change the register into number
        public static Integer getRegisterNumber(String register){
            for(Register r: Register.values()){
                if(r.name.equals(register))
                    return r.number;  
            }
            return null;
        }
}

