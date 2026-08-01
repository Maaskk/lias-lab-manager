import { describe, expect, it } from 'vitest';
import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const appSource = readFileSync(join(here, 'App.jsx'), 'utf8');
const frontendRoot = join(here, '..');
const projectRoot = join(frontendRoot, '..');
const productionEnvPath = join(frontendRoot, '.env.production');
const productionEnv = existsSync(productionEnvPath) ? readFileSync(productionEnvPath, 'utf8') : '';
const nginxSource = readFileSync(join(frontendRoot, 'nginx.conf'), 'utf8');
const dockerCompose = readFileSync(join(projectRoot, 'docker-compose.yml'), 'utf8');
const securitySource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'config', 'SecurityConfig.java'), 'utf8');
const dataLoaderSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'config', 'DataLoader.java'), 'utf8');
const appYaml = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'resources', 'application.yml'), 'utf8');
const adminControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'AdminController.java'), 'utf8');
const memberControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'MemberController.java'), 'utf8');
const membershipControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'MembershipRequestController.java'), 'utf8');
const documentControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'DocumentController.java'), 'utf8');
const eventControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'EventController.java'), 'utf8');
const publicationControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'PublicationController.java'), 'utf8');
const materialRequestControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'MaterialRequestController.java'), 'utf8');
const activeMandateServiceSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'service', 'ActiveMandateService.java'), 'utf8');
const reportServiceSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'service', 'ReportService.java'), 'utf8');
const searchControllerSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'SearchController.java'), 'utf8');

describe('LIAS workflow contracts', () => {
  it('submits public membership requests to the unauthenticated multipart endpoint', () => {
    expect(appSource).toContain("api.post('/membership-requests/public'");
    expect(appSource).toContain("fd.set('emailAddress'");
  });

  it('uses backend report and notification contracts that exist in Spring controllers', () => {
    expect(appSource).toContain("useFetch('/reports'");
    expect(appSource).toContain("api.post('/reports/generate'");
    expect(appSource).toContain("api.patch('/notifications/read-all'");
  });

  it('contains concrete internal workflow screens instead of JSON-only placeholders', () => {
    expect(appSource).toContain('function GlobalSearch');
    expect(appSource).toContain('function ProfileEditor');
    expect(appSource).toContain('function AdhesionActions');
    expect(appSource).toContain('function MessageComposer');
    expect(appSource).toContain('function MeetingPvUpload');
    expect(appSource).toContain('function ConventionDocumentUpload');
  });

  it('is deployable behind the frontend container without browser localhost API calls', () => {
    expect(nginxSource).toContain('location /api/');
    expect(nginxSource).toContain('proxy_pass http://backend:8080/api/');
    expect(nginxSource).toContain('location /uploads/');
    expect(dockerCompose).toContain('VITE_API_URL: /api');
  });

  it('has production security headers and no hard-coded deployment secret fallback', () => {
    expect(securitySource).toContain('contentSecurityPolicy');
    expect(securitySource).toContain('httpStrictTransportSecurity');
    expect(securitySource).toContain('frameOptions');
    expect(appYaml).toContain('secret: ${JWT_SECRET:}');
  });

  it('exposes the original LIAS public website sections and full governance pages', () => {
    expect(appSource).toContain('function PublicProjects');
    expect(appSource).toContain('function PublicPartners');
    expect(appSource).toContain('function Governance');
    expect(appSource).toContain('function RoleHistoryAdmin');
    expect(appSource).toContain('function AffiliationAdmin');
  });

  it('does not expose local demo credentials in the login screen source', () => {
    expect(appSource).not.toContain('defaultValue="admin@lias.ma"');
    expect(appSource).not.toContain('defaultValue="Admin123!"');
    expect(appSource).not.toContain('Compte admin local');
    expect(appSource).not.toContain('admin@lias.ma / Admin123!');
  });

  it('does not hard-code seeded account passwords for deployable installs', () => {
    expect(dataLoaderSource).not.toContain('Admin123!');
    expect(dataLoaderSource).toContain('initialPassword');
    expect(appYaml).toContain('initial-password: ${INITIAL_ADMIN_PASSWORD:}');
    expect(dockerCompose).toContain('INITIAL_ADMIN_PASSWORD');
  });

  it('scrolls public hash navigation targets after React Router updates the URL', () => {
    expect(appSource).toContain('function HashScroll');
    expect(appSource).toContain('location.hash.slice(1)');
    expect(appSource).toContain('scrollIntoView');
    expect(appSource).toContain('<HashScroll/>');
  });

  it('builds production frontend bundles against the nginx /api proxy', () => {
    expect(productionEnv).toContain('VITE_API_URL=/api');
  });

  it('ships a root backend source tree for docker and tests', () => {
    expect(existsSync(join(projectRoot, 'backend', 'pom.xml'))).toBe(true);
    expect(existsSync(join(projectRoot, 'backend', 'Dockerfile'))).toBe(true);
    expect(existsSync(join(projectRoot, 'backend', 'target'))).toBe(false);
  });

  it('lets admins change user role and status from the admin users screen', () => {
    expect(appSource).toContain('function AdminUsers');
    expect(appSource).toContain('api.patch(`/admin/users/${id}/role');
    expect(appSource).toContain('api.patch(`/admin/users/${id}/status');
    expect(appSource).toContain('AppRole');
    expect(appSource).toContain('UserStatus');
    expect(adminControllerSource).toContain('@PatchMapping("/users/{id}/role")');
    expect(adminControllerSource).toContain('@PatchMapping("/users/{id}/status")');
  });

  it('filters portal navigation by role so doctorants and associates do not see forbidden modules', () => {
    expect(appSource).toContain('const roleNavigation');
    expect(appSource).toContain('DOCTORAL');
    expect(appSource).toContain('/publications');
    expect(appSource).toContain('ASSOCIATE_MEMBER');
    expect(appSource).toContain('filterNavForRole');
  });

  it('accepts membership requests with director-selected member type and app role', () => {
    expect(appSource).toContain('memberType');
    expect(appSource).toContain('appRole');
    expect(membershipControllerSource).toContain('decision.memberType()');
    expect(membershipControllerSource).toContain('decision.appRole()');
    expect(membershipControllerSource).not.toContain('u.setAppRole(AppRole.DOCTORAL);');
    expect(membershipControllerSource).not.toContain('m.setType(MemberType.DOCTORAL);');
  });

  it('has explicit member lifecycle workflows for retirement, former status, and reactivation', () => {
    expect(memberControllerSource).toContain('@PatchMapping("/{id}/retire")');
    expect(memberControllerSource).toContain('@PatchMapping("/{id}/mark-former")');
    expect(memberControllerSource).toContain('@PatchMapping("/{id}/reactivate")');
    expect(memberControllerSource).toContain('MemberLifecycleService');
    expect(appSource).toContain('retireMember');
    expect(appSource).toContain('markFormerMember');
    expect(appSource).toContain('reactivateMember');
  });

  it('sets document uploadedBy from the authenticated user', () => {
    expect(documentControllerSource).toContain('Authentication auth');
    expect(documentControllerSource).toContain('d.setUploadedBy(u.getId())');
  });

  it('search maps members to safe fields instead of returning raw Member entities', () => {
    expect(searchControllerSource).toContain('safeMember');
    expect(searchControllerSource).not.toContain('\"members\", members.findAll().stream()');
    expect(searchControllerSource).not.toContain('birthDate');
  });

  it('annual report generation filters yearly modules by selected year', () => {
    expect(reportServiceSource).toContain('eventsForYear');
    expect(reportServiceSource).toContain('publicationsForYear');
    expect(reportServiceSource).toContain('meetingsForYear');
    expect(reportServiceSource).not.toContain('events.count()');
    expect(reportServiceSource).not.toContain('publications.count()');
  });

  it('enforces active mandate validation for adhesion decisions and mandate overlap prevention', () => {
    expect(activeMandateServiceSource).toContain('requireActiveDirectorOrAdmin');
    expect(activeMandateServiceSource).toContain('validateNoOverlap');
    expect(membershipControllerSource).toContain('activeMandates.requireActiveDirectorOrAdmin(actor)');
  });

  it('has real event and document detail screens with authenticated file opening', () => {
    expect(appSource).toContain('function EventDetail');
    expect(appSource).toContain('function DocumentDetail');
    expect(appSource).toContain('function SecureFileButton');
    expect(appSource).toContain('<EventDetail/>');
    expect(appSource).toContain('<DocumentDetail/>');
    expect(eventControllerSource).toContain('documents.findByEventId(id)');
    expect(eventControllerSource).toContain('messages.findByEventIdOrderBySentAtAsc(id)');
    expect(documentControllerSource).toContain('nextVersion');
    expect(documentControllerSource).toContain('d.setArchived(true)');
  });

  it('connects material approval to distributions, stock decrement, reasons, and notifications', () => {
    expect(appSource).toContain('/material-distributions');
    expect(appSource).toContain('reviewReason');
    expect(materialRequestControllerSource).toContain('distributeApprovedRequest');
    expect(materialRequestControllerSource).toContain('item.setQuantity(item.getQuantity()-request.getQuantity())');
    expect(materialRequestControllerSource).toContain('notifyRequester');
  });

  it('sets publication ownership and enriches profile screens', () => {
    expect(publicationControllerSource).toContain('body.setAddedBy(u.getId())');
    expect(appSource).toContain('uploadPhoto');
    expect(appSource).toContain('teamName');
    expect(appSource).toContain('data.publications');
  });

  it('protects internal uploads instead of permitting every uploaded file publicly', () => {
    expect(securitySource).toContain('/uploads/public/**');
    expect(securitySource).toContain('/uploads/photos/**');
    expect(securitySource).toContain('.requestMatchers("/uploads/**").authenticated()');
    expect(securitySource).not.toContain('"/uploads/**", "/error").permitAll()');
  });
});
