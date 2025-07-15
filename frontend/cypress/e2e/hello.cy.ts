describe('Hello E2E Test', () => {
  it('방문하면 Hello, World!가 보인다', () => {
    cy.visit('/');
    cy.contains('Hello, World!').should('be.visible');
  });
});
