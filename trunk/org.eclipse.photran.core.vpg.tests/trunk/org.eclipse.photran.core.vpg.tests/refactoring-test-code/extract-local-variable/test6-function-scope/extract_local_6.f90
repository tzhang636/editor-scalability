program extract_local
    implicit none
    print *, f_to_c(212.)
    print *, f_to_c(32.)
end program

REAL FUNCTION f_to_c(f_temp)
    REAL, INTENT(IN) :: f_temp
    f_to_c = (f_temp - 32)/1.8 !<<<<< 9, 14, 9, 31, real :: c_temp, pass
END FUNCTION f_to_c
