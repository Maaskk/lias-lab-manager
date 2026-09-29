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
const gitignoreSource = readFileSync(join(projectRoot, '.gitignore'), 'utf8');
const securitySource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'config', 'SecurityConfig.java'), 'utf8');
const dataLoaderSource = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'config', 'DataLoader.java'), 'utf8');
const appYaml = readFileSync(join(projectRoot, 'backend', 'src', 'main', 'resources', 'application.yml'), 'utf8');
const indexCss = readFileSync(join(frontendRoot, 'src', 'index.css'), 'utf8');
const testAccountsPath = join(projectRoot, 'TEST_ACCOUNTS.md');
const testAccountsSource = existsSync(testAccountsPath) ? readFileSync(testAccountsPath, 'utf8') : '';
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
const calendarControllerPath = join(projectRoot, 'backend', 'src', 'main', 'java', 'ma', 'lias', 'controller', 'CalendarController.java');
const calendarControllerSource = existsSync(calendarControllerPath) ? readFileSync(calendarControllerPath, 'utf8') : '';
const envFiles = ['.env', '.env.example']
  .map((name) => [name, join(projectRoot, name)])
  .filter(([, path]) => existsSync(path))
  .map(([name, path]) => [name, readFileSync(path, 'utf8')]);
const envValue = (source, key) => source.split(/\r?\n/).find((line) => line.startsWith(`${key}=`))?.split('=').slice(1).join('=') || '';

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
    expect(nginxSource).toContain('location ^~ /uploads/');
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

  it('clears the previous protected route when signing out', () => {
    expect(appSource).toContain("navigate('/login',{replace:true})");
    expect(appSource).toContain('onClick={signOut}');
  });

  it('keeps form references valid across asynchronous submissions', () => {
    expect(appSource).not.toContain('e.currentTarget.reset()');
    expect(appSource).toContain('const form=e.currentTarget');
    expect(appSource).toContain('form.reset()');
  });

  it('documents complete seeded test accounts outside the login form', () => {
    expect(testAccountsSource).toContain('admin@lias.ma');
    expect(testAccountsSource).toContain('faouzia.benabbou@lias.local');
    expect(testAccountsSource).toContain('permanent.demo@lias.local');
    expect(testAccountsSource).toContain('doctorant.demo@lias.local');
    expect(testAccountsSource).toContain('associe.demo@lias.local');
    expect(testAccountsSource).toContain('INITIAL_ADMIN_PASSWORD');
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
    expect(gitignoreSource).toContain('backend/target/');
  });

  it('lets admins change user role and status from the admin users screen', () => {
    expect(appSource).toContain('function AdminUsers');
    expect(appSource).toContain('createUser');
    expect(appSource).toContain('resetPassword');
    expect(appSource).toContain('api.patch(`/admin/users/${id}/role');
    expect(appSource).toContain('api.patch(`/admin/users/${id}/status');
    expect(appSource).toContain('api.patch(`/admin/users/${id}/password');
    expect(appSource).not.toContain('/password?password=');
    expect(appSource).toContain("api.post('/admin/users'");
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
    expect(appSource).toContain('const INTERNAL_ROLES');
    expect(appSource).toContain('const DOCUMENT_ROLES');
    expect(appSource).toContain('const PUBLICATION_ROLES');
    expect(appSource).toContain('path="/dashboard" element={<Protected roles={INTERNAL_ROLES}>');
    expect(appSource).toContain('path="/membres" element={<Protected roles={INTERNAL_ROLES}>');
    expect(appSource).toContain("['DOCTORAL','RETIRED','FORMER'].includes(u.role)?'/profil'");
  });

  it('seeds role-specific accounts and demo workflow data for testing', () => {
    expect(dataLoaderSource).toContain('permanent.demo@lias.local');
    expect(dataLoaderSource).toContain('doctorant.demo@lias.local');
    expect(dataLoaderSource).toContain('associe.demo@lias.local');
    expect(dataLoaderSource).toContain('MessageRepository messages');
    expect(dataLoaderSource).toContain('MaterialInventoryRepository materialInventory');
    expect(dataLoaderSource).toContain('MaterialRequestRepository materialRequests');
    expect(dataLoaderSource).toContain('message(');
    expect(dataLoaderSource).toContain('material(');
    expect(dataLoaderSource).toContain('materialRequest(');
  });

  it('supports global, direct, team, and event conversations from the messages screen', () => {
    expect(appSource).toContain('messageScope');
    expect(appSource).toContain('receiverId');
    expect(appSource).toContain('teamId');
    expect(appSource).toContain('eventId');
    expect(appSource).toContain('messageType');
    expect(appSource).toContain('/messages?receiverId=');
    expect(appSource).toContain('/messages?teamId=');
    expect(appSource).toContain('/messages?eventId=');
  });

  it('uses a professional institutional visual system instead of bubbly template styling', () => {
    expect(indexCss).toContain('--brand-ink');
    expect(indexCss).toContain('--brand-teal');
    expect(indexCss).toContain('border-radius: 8px');
    expect(indexCss).toContain('.professional-surface');
    expect(appSource).toContain('professional-surface');
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
    expect(reportServiceSource).toContain('Répartition des membres');
    expect(reportServiceSource).toContain('Activités scientifiques');
    expect(reportServiceSource).toContain('Conventions et partenariats');
    expect(reportServiceSource).not.toContain('events.count()');
    expect(reportServiceSource).not.toContain('publications.count()');
  });

  it('enforces active mandate validation for adhesion decisions and mandate overlap prevention', () => {
    expect(activeMandateServiceSource).toContain('requireActiveDirectorOrAdmin');
    expect(activeMandateServiceSource).toContain('validateNoOverlap');
    expect(membershipControllerSource).toContain('activeMandates.requireActiveDirector(actor)');
    expect(membershipControllerSource).not.toContain('legacy contract marker');
  });

  it('ships real calendar aggregation instead of event cards only', () => {
    expect(calendarControllerSource).toContain('@RequestMapping("/api/calendar")');
    expect(calendarControllerSource).toContain('meetings.findAll()');
    expect(calendarControllerSource).toContain('mandates.findAll()');
    expect(appSource).toContain("useFetch(`/calendar?");
    expect(appSource).toContain('calendarDays');
    expect(appSource).toContain('Réunions');
  });

  it('uses backend public publications when available instead of static-only public data', () => {
    expect(appSource).toContain("api.get('/public/publications'");
    expect(appSource).toContain('backendPublications');
    expect(appSource).toContain('publicationUrl');
  });

  it('uses inline decision forms instead of browser prompt dialogs', () => {
    expect(appSource).not.toContain('window.prompt');
    expect(appSource).toContain('decisionNotes');
    expect(appSource).toContain('rejectionReason');
  });

  it('exposes material fairness data in the UI', () => {
    expect(appSource).toContain("useFetch('/material/equity'");
    expect(appSource).toContain('membersWithoutMaterial');
    expect(appSource).toContain('Membres sans matériel');
  });

  it('paginates global search responses with result metadata', () => {
    expect(searchControllerSource).toContain('@RequestParam(defaultValue="20") int limit');
    expect(searchControllerSource).toContain('page');
    expect(searchControllerSource).toContain('total');
    expect(searchControllerSource).toContain('paginate(');
  });

  it('keeps deployment secrets valid and generated artifacts out of the submitted tree', () => {
    for (const [name, source] of envFiles) {
      expect(envValue(source, 'JWT_SECRET').length, `${name} JWT_SECRET length`).toBeGreaterThanOrEqual(64);
      expect(envValue(source, 'INITIAL_ADMIN_PASSWORD').length, `${name} INITIAL_ADMIN_PASSWORD length`).toBeGreaterThanOrEqual(24);
    }
    expect(existsSync(join(projectRoot, 'docs', 'backend', 'target'))).toBe(false);
    expect(existsSync(join(projectRoot, 'docs', 'backend', 'pom.xml'))).toBe(false);
    expect(existsSync(join(projectRoot, '.gitignore'))).toBe(true);
  });

  it('has real event and document detail screens with authenticated file opening', () => {
    expect(appSource).toContain('function EventDetail');
    expect(appSource).toContain('function DocumentDetail');
    expect(appSource).toContain('function SecureFileButton');
    expect(appSource).toContain('<EventDetail/>');
    expect(appSource).toContain('<DocumentDetail/>');
    expect(eventControllerSource).toContain('documents.findByEventId(id)');
    expect(eventControllerSource).toContain('messages.findByEventIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(id,MessageType.EVENT)');
    expect(eventControllerSource).not.toContain('legacy contract marker');
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
    expect(publicationControllerSource).toContain('body.setAddedBy(m.getId())');
    expect(publicationControllerSource).toContain('canModify');
    expect(publicationControllerSource).not.toContain('legacy marker');
    expect(appSource).toContain('uploadPhoto');
    expect(appSource).toContain('teamName');
    expect(appSource).toContain('data.publications');
  });

  it('shows uploaded profile photos with previews and reusable avatar rendering', () => {
    expect(appSource).toContain('function ProfileAvatar');
    expect(appSource).toContain('photoPreviewUrl');
    expect(appSource).toContain('URL.createObjectURL');
    expect(appSource).toContain('Photo actuelle');
    expect(appSource).toContain('<ProfileAvatar member={data}');
    expect(appSource).toContain('<ProfileAvatar member={row}');
  });

  it('lets users start direct messages by choosing members instead of typing raw ids', () => {
    expect(appSource).toContain('membersByUserId');
    expect(appSource).toContain('selectedReceiver');
    expect(appSource).toContain('Choisir un membre');
    expect(appSource).toContain('name="receiverId"');
    expect(appSource).toContain('senderName');
    expect(appSource).toContain('receiverName');
    expect(appSource).toContain("useFetch('/teams'");
    expect(appSource).toContain("useFetch('/events'");
  });

  it('protects internal uploads instead of permitting every uploaded file publicly', () => {
    expect(securitySource).toContain('/uploads/public/**');
    expect(securitySource).toContain('/uploads/photos/**');
    expect(securitySource).toContain('.requestMatchers("/uploads/**").authenticated()');
    expect(securitySource).toContain('HttpMethod.POST, "/api/members/*/photo"');
    expect(securitySource).not.toContain('"/uploads/**", "/error").permitAll()');
  });
});
