program commentIfStmtToIfConstruct
    implicit none
    integer :: x, y, a
    if (x .LT. y .OR. y .GT. 5 .AND. 6 .GE. 6) a = 1 !This is an if statement
    print *, "This is a test" !<<<<< 4, 5, 4, 78, pass
    
    !!! This test shows the refactoring successfully converting a valid IF statement to a valid IF construct, while
    !!! also preserving the included comment.
end program