program test4!<<<<< 8,5,10,11,fail-initial
    implicit none
    integer, parameter :: N=100000
    real v(N)
	integer i


    do i=1,N
       v(i)=v(i+1)
    end do
end program test4
