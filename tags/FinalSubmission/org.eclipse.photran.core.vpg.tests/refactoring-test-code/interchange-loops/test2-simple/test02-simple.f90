program main
	integer :: i, j
	integer :: k, z
	
	
	do j = 1, 10 !<<<<< 6, 5, 14, 11, pass
	    do i = 2, 15
	        print *, i                   
	        if (i .gt. j) then
	            print *, i * 10
	        end if                      
	        print *, i
	    end do
	end do
	

end program main
